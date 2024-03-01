package test;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class MessageGrouping {

    public String evaluate(String sortedMessage, String sortedOrder) {
        //初始化字符串，将字符串转化为list<map<>>类型
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            List<Map<String, String>> listMap_1 = objectMapper.readValue(sortedMessage, new TypeReference<List<Map<String, String>>>() {
            });
            //对空值处理
            if (!sortedOrder.equals("")) {
                List<Map<String, String>> listMap_2 = objectMapper.readValue(sortedOrder, new TypeReference<List<Map<String, String>>>() {
                });
                return groupMessages(listMap_1, listMap_2);
            }
            else {
                return groupMessages(listMap_1, new ArrayList<>());
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String groupMessages(List<Map<String, String>> sortedMessage, List<Map<String, String>> sortedOrder) throws JSONException {
        List<Map<String, String>> sortedMessages = sortbytime(sortedMessage, "create_time");
        List<Map<String, String>> sortedOrders=sortbytime(sortedOrder, "create_time");
        List<List<Map<String, String>>> groupedMessages = new ArrayList<>();
        List<Map<String, String>> currentGroup = new ArrayList<>();
        LocalDateTime currentGroupEnd = null;
        int key=0;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (Map<String, String> msgRow : sortedMessages) {

            if (currentGroupEnd == null || (LocalDateTime.parse((String)msgRow.get("create_time"), formatter)).isAfter(currentGroupEnd)) {
                // 新分组的第一条消息，尝试关联订单
                currentGroup = new ArrayList<>();
                currentGroup.add(msgRow);
                LocalDateTime dateTime = LocalDateTime.parse((String)msgRow.get("create_time"), formatter);
                LocalDateTime createDateTime = dateTime;
                LocalDateTime startDateTime = createDateTime.minus(30, ChronoUnit.DAYS);
                LocalDateTime endDateTime = createDateTime.plus(30, ChronoUnit.DAYS);
                List<Map<String, String>> relatedOrders = findRelatedOrders(sortedOrders, startDateTime, endDateTime);

                if (!relatedOrders.isEmpty()) {
                    LocalDateTime latestCheckout = findLatestCheckoutDate(relatedOrders);
                    currentGroupEnd = latestCheckout.plus(30, ChronoUnit.DAYS);
                    // 将关联的订单加入到当前分组
                    for (Map<String, String> orderRow : relatedOrders) {
                        currentGroup.add(orderRow);
                    }
                    key=1;
                } else {
                    currentGroupEnd = createDateTime.plus(30, ChronoUnit.DAYS);
                    key=0;
                }
                // 如果是新的单独分组，立即添加
                if (groupedMessages.isEmpty() || LocalDateTime.parse((String)msgRow.get("create_time"), formatter).isAfter(LocalDateTime.parse((String)groupedMessages.get(groupedMessages.size() - 1).get(groupedMessages.get(groupedMessages.size() - 1).size() - 1).get("create_time"),formatter)  )) {
                    groupedMessages.add(currentGroup);
                }
            } else if ((LocalDateTime.parse((String)msgRow.get("create_time"), formatter)).isBefore(currentGroupEnd) || (LocalDateTime.parse((String)msgRow.get("create_time"), formatter)).isEqual(currentGroupEnd)) {
                // 如果消息在当前分组的时间范围内，添加到当前分组
                currentGroup.add(msgRow);
                if(key==0) {
                    currentGroupEnd = LocalDateTime.parse((String) msgRow.get("create_time"), formatter).plus(30, ChronoUnit.DAYS);
                }
            }
        }
        return formatAndPrintGroupedMessages(groupedMessages);
    }

    public static List<Map<String, String>> sortbytime(List<Map<String, String>> list,String key){
        return list.stream()
                .sorted(Comparator.comparing(map -> (String) map.get(key)))
                .collect(Collectors.toList());
    }

    public static String formatAndPrintGroupedMessages(List<List<Map<String, String>>> groupedMessages) throws JSONException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        List<String> list=new ArrayList<>();
        for (int i = 0; i < groupedMessages.size(); i++) {
            Map<String,String> map=new HashMap<>();
            List<Map<String, String>> group = groupedMessages.get(i);
            // 起始日期是分组中第一条消息的日期
            LocalDate startTime = LocalDate.parse((String)group.get(0).get("create_time"), formatter);

            // 结束日期是分组中最后一条消息的日期或最大的订单离店日期+30天
            LocalDate endTime = LocalDate.parse((String)group.get(group.size() - 1).get("create_time"), formatter);//.plusDays(30);
            /*if (!group.get(group.size() - 1).get("checkout_date").equals("")) {
                LocalDate checkoutDate = LocalDate.parse((String)group.get(group.size() - 1).get("checkout_date"),formatter);
                endTime = endTime.isAfter(checkoutDate.plusDays(30)) ? endTime : checkoutDate.plusDays(30);
            }*/
            // 检查是否有比这更晚的订单离店日期
            for (Map<String, String> msg : group) {
                if (!msg.get("checkout_date").equals("")) {
                    LocalDate potentialEndTime =LocalDate.parse((String)msg.get("checkout_date"), formatter);
                    if (potentialEndTime.plusDays(30).isAfter(endTime)) {
                        endTime = potentialEndTime;
                    }
                }
            }

            // 计算消息数量和订单数量
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", String.valueOf(i + 1));
            jsonObject.put("startdate", startTime);
            jsonObject.put("checkout_date", endTime.plusDays(30));
            list.add(jsonObject.toString());

        }
        return list.toString();
    }



    private static List<Map<String, String>> findRelatedOrders(List<Map<String, String>> orders, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        List<Map<String, String>> relatedOrders = new ArrayList<>();

        for (Map<String, String> orderRow : orders) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime dateTime = LocalDateTime.parse((String)orderRow.get("create_time"), formatter);
            LocalDateTime createDateTime = dateTime;
            if (createDateTime.isAfter(startDateTime) && createDateTime.isBefore(endDateTime)) {
                relatedOrders.add(orderRow);
            }
        }

        return relatedOrders;
    }

    private static LocalDateTime findLatestCheckoutDate(List<Map<String, String>> orders) {
        LocalDateTime latestCheckout = null;
        for (Map<String, String> orderRow : orders) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime checkoutDate = LocalDateTime.parse((String)orderRow.get("checkout_date"), formatter);;
            if (latestCheckout == null || checkoutDate.isAfter(latestCheckout)) {
                latestCheckout = checkoutDate;
            }
        }
        return latestCheckout;
    }



    public static void main(String[] args) {
        StringBuilder jsonString_1 = new StringBuilder();
        jsonString_1.append(
                "[{\"checkout_date\": \"\", \"create_time\": \"2023-06-20 14:23:26\", \"id\": \"2249680101145512320\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-04-22 18:48:22\", \"id\": \"1106250684900608384\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-05-07 06:13:20\", \"id\": \"1127233917159012736\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-05-23 09:38:22\", \"id\": \"1150633136267901312\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-07-30 12:08:58\", \"id\": \"1249354235540892032\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-09-18 21:57:06\", \"id\": \"2380596409285482880\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-09-18 22:06:39\", \"id\": \"2380606022915459456\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-09-18 22:10:14\", \"id\": \"2380609620437207424\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-09-18 21:54:01\", \"id\": \"2380593295534950784\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-09-18 22:05:56\", \"id\": \"2380605292838136192\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-09-18 21:55:36\", \"id\": \"2380594898363095424\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-04-29 19:28:52\", \"id\": \"1116438310802815360\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-05-23 09:44:03\", \"id\": \"1150638857734797696\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-06-10 05:58:59\", \"id\": \"2234676792034810240\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-05-07 06:17:47\", \"id\": \"1127238397279857024\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-06-02 17:26:44\", \"id\": \"2223772688479525248\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-09-18 21:54:24\", \"id\": \"2380593683206228352\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-07-31 06:28:40\", \"id\": \"1250461224236878208\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-09-18 21:56:22\", \"id\": \"2380595666759686528\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-06-10 05:59:08\", \"id\": \"2234676944069810560\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-06-06 20:47:42\", \"id\": \"2229773192724703616\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-07-30 12:07:33\", \"id\": \"1249352798974994816\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-06-09 20:21:53\", \"id\": \"2234095855697865088\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-05-23 09:42:53\", \"id\": \"1150637683396749696\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-07-30 12:07:24\", \"id\": \"1249352647677876608\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-05-23 09:44:14\", \"id\": \"1150639032939260288\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-04-22 18:45:45\", \"id\": \"1106248052219869568\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-07-30 12:08:12\", \"id\": \"1249353459695327616\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-05-07 06:24:59\", \"id\": \"1127245633494206848\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-06-07 22:16:45\", \"id\": \"2231312389952473472\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-09-18 21:53:23\", \"id\": \"2380592669409233280\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-06-07 22:17:07\", \"id\": \"2231312745948211584\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-07-31 06:28:05\", \"id\": \"1250460641799149952\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-06-11 07:03:15\", \"id\": \"2236191035838146944\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-05-07 06:33:57\", \"id\": \"1127254665760205184\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-05-30 18:51:07\", \"id\": \"2219508982153415040\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-09-18 21:48:35\", \"id\": \"2380587826145917312\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-06-07 22:17:41\", \"id\": \"2231313323319331200\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-10-31 19:59:46\", \"id\": \"2442809000854047104\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-05-07 06:12:41\", \"id\": \"1127233254240876928\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-09-18 21:56:28\", \"id\": \"2380595767305329024\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-06-10 06:05:42\", \"id\": \"2234683549226203520\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-05-23 09:41:01\", \"id\": \"1150635795171453312\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-06-06 20:48:09\", \"id\": \"2229773642236668288\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-06-04 17:30:28\", \"id\": \"2226675553393154432\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-06-10 05:58:29\", \"id\": \"2234676285899635072\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-05-30 21:07:05\", \"id\": \"2219645837679225216\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-06-10 05:59:59\", \"id\": \"2234677790547601792\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-10-30 21:58:24\", \"id\": \"2441478873306339712\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-05-23 09:42:15\", \"id\": \"1150637039487195520\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-10-31 20:00:34\", \"id\": \"2442809810539387264\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-06-06 20:54:19\", \"id\": \"2229779852591581568\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-09-18 21:55:48\", \"id\": \"2380595102860446080\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-07-30 12:06:29\", \"id\": \"1249351727196014976\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-05-23 09:43:02\", \"id\": \"1150637835700341120\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-05-23 09:37:43\", \"id\": \"1150632479523780992\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-05-07 06:12:53\", \"id\": \"1127233467026381184\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-05-07 06:29:12\", \"id\": \"1127249891920726400\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-07-30 12:06:31\", \"id\": \"1249351767008344448\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-09-18 21:53:55\", \"id\": \"2380593194049616256\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-05-23 09:43:29\", \"id\": \"1150638287208753536\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-05-23 09:42:40\", \"id\": \"1150637452693277056\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-05-23 09:41:10\", \"id\": \"1150635956752783744\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-09-18 21:56:41\", \"id\": \"2380595985509800320\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-06-07 22:17:38\", \"id\": \"2231313269498030464\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-07-30 12:07:02\", \"id\": \"1249352290558146944\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-07-30 12:08:08\", \"id\": \"1249353382335469952\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-06-11 07:04:10\", \"id\": \"2236191957846817152\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-06-11 07:23:16\", \"id\": \"2236211187489163648\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-10-22 20:34:16\", \"id\": \"2429797768224287104\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-06-11 07:13:52\", \"id\": \"2236201725156075904\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-08-17 23:51:21\", \"id\": \"2334325769364842880\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-07-30 12:10:32\", \"id\": \"1249355809445091712\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-06-11 07:12:32\", \"id\": \"2236200369942698368\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-09-18 21:52:27\", \"id\": \"2380591730254285184\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-05-07 05:51:11\", \"id\": \"1127211610642446720\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-07-30 12:05:48\", \"id\": \"1249351042635258240\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-05-23 09:37:16\", \"id\": \"1150632014895556992\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-04-29 19:25:12\", \"id\": \"1116434616292088192\"}, {\"checkout_date\": \"\", \"create_time\": \"2020-09-27 17:39:35\", \"id\": \"806124296472181120\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-10-22 20:34:26\", \"id\": \"2429797930392648064\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-05-23 09:41:57\", \"id\": \"1150636744694745472\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-06-15 09:28:43\", \"id\": \"2242135671886784896\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-06-05 06:51:08\", \"id\": \"2227481523685906816\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-10-22 20:39:14\", \"id\": \"2429802763640142208\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-05-30 18:18:00\", \"id\": \"2219475645842024832\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-05-23 09:44:02\", \"id\": \"1150638832183085440\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-09-18 21:53:14\", \"id\": \"2380592519672723840\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-10-31 20:13:05\", \"id\": \"2442822416855632256\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-07-31 05:49:13\", \"id\": \"1250421515200719232\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-05-07 05:53:24\", \"id\": \"1127213845686380928\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-05-23 09:44:11\", \"id\": \"1150638980728531328\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-06-07 22:17:19\", \"id\": \"2231312948918974848\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-05-07 06:15:42\", \"id\": \"1127236302291880320\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-05-30 18:18:13\", \"id\": \"2219475855188109696\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-10-31 20:13:15\", \"id\": \"2442822569897253248\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-09-18 21:55:03\", \"id\": \"2380594342147918208\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-09-18 21:47:39\", \"id\": \"2380586887561337216\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-05-23 09:38:28\", \"id\": \"1150633228391614848\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-06-07 22:17:28\", \"id\": \"2231313109829253504\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-04-29 19:25:05\", \"id\": \"1116434501988907392\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-06-07 22:16:21\", \"id\": \"2231311987014089088\"}, {\"checkout_date\": \"\", \"create_time\": \"2020-09-27 17:40:23\", \"id\": \"806125091712219520\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-06-07 22:16:21\", \"id\": \"2231311985302776192\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-06-10 05:58:42\", \"id\": \"2234676505714628992\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-07-30 12:28:29\", \"id\": \"1249373877349095808\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-06-07 22:17:30\", \"id\": \"2231313139323623808\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-05-07 06:32:11\", \"id\": \"1127252893717105024\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-09-18 22:05:08\", \"id\": \"2380604495081765248\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-06-10 05:59:29\", \"id\": \"2234677284026583424\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-09-18 21:47:49\", \"id\": \"2380587054729275776\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-06-04 18:05:59\", \"id\": \"2226711291161696640\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-05-07 06:18:18\", \"id\": \"1127238921483970944\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-05-30 21:07:05\", \"id\": \"2219645839658953088\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-04-22 18:45:12\", \"id\": \"1106247495417608576\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-07-30 12:08:45\", \"id\": \"1249354011799837056\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-05-23 09:37:16\", \"id\": \"1150632016103516544\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-06-06 20:46:52\", \"id\": \"2229772358091762048\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-05-23 09:42:02\", \"id\": \"1150636821853153664\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-06-07 22:17:12\", \"id\": \"2231312843708889472\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-06-08 06:02:58\", \"id\": \"2231781700693379456\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-07-30 12:21:08\", \"id\": \"1249366470409550208\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-05-23 09:38:09\", \"id\": \"1150632906050939264\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-04-29 19:28:25\", \"id\": \"1116437853388806528\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-06-06 20:46:39\", \"id\": \"2229772130106165632\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-06-11 07:03:16\", \"id\": \"2236191039797336448\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-06-10 05:59:13\", \"id\": \"2234677019449977216\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-05-23 09:41:43\", \"id\": \"1150636495016245632\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-06-08 21:41:58\", \"id\": \"2232726926056425856\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-05-23 09:37:37\", \"id\": \"1150632380135549312\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-05-23 09:41:13\", \"id\": \"1150635995575273856\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-06-04 17:30:31\", \"id\": \"2226675593742350720\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-05-07 06:33:23\", \"id\": \"1127254093841701248\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-06-06 20:47:51\", \"id\": \"2229773338988456320\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-06-10 05:59:23\", \"id\": \"2234677187020802432\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-05-07 06:16:16\", \"id\": \"1127236868288276864\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-06-04 18:05:30\", \"id\": \"2226710808581843328\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-06-11 07:04:54\", \"id\": \"2236192696681863552\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-09-18 21:47:02\", \"id\": \"2380586270327310720\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-09-18 22:06:59\", \"id\": \"2380606349635189120\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-05-30 18:21:41\", \"id\": \"2219479347550185856\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-07-30 12:07:37\", \"id\": \"1249352863818844544\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-07-31 05:48:49\", \"id\": \"1250421112111331712\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-05-30 18:48:40\", \"id\": \"2219506510668216704\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-05-23 09:38:01\", \"id\": \"1150632774601447808\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-05-23 09:38:13\", \"id\": \"1150632985759525248\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-06-11 07:12:32\", \"id\": \"2236200371989744000\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-07-31 05:48:29\", \"id\": \"1250420777892415872\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-06-06 20:56:42\", \"id\": \"2229782252102609280\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-09-18 21:56:07\", \"id\": \"2380595406226209152\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-10-31 20:24:17\", \"id\": \"2442833677991405952\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-06-09 20:21:53\", \"id\": \"2234095853684607360\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-06-10 05:59:43\", \"id\": \"2234677527363324288\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-08-17 23:51:46\", \"id\": \"2334326189063756160\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-05-30 21:08:18\", \"id\": \"2219647071962212736\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-09-18 21:53:49\", \"id\": \"2380593103721023872\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-04-29 19:29:17\", \"id\": \"1116438725653092736\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-04-22 18:50:48\", \"id\": \"1106253140682353024\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-07-30 12:07:12\", \"id\": \"1249352444908534144\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-05-30 18:21:53\", \"id\": \"2219479552836176256\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-10-22 20:37:50\", \"id\": \"2429801360914119040\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-06-15 09:28:43\", \"id\": \"2242135669890320768\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-06-11 07:13:53\", \"id\": \"2236201727169350016\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-09-18 22:05:26\", \"id\": \"2380604797876803968\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-07-31 06:28:05\", \"id\": \"1250460643761887616\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-06-07 22:17:05\", \"id\": \"2231312711169059200\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-06-04 17:29:58\", \"id\": \"2226675046469478784\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-05-07 06:33:34\", \"id\": \"1127254278206454144\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-09-18 21:52:53\", \"id\": \"2380592160002550144\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-10-22 20:39:05\", \"id\": \"2429802616185399680\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-06-20 14:23:26\", \"id\": \"2249680103125260672\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-09-18 21:48:06\", \"id\": \"2380587336603523456\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-05-30 21:07:28\", \"id\": \"2219646224612161920\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-06-11 07:04:51\", \"id\": \"2236192647574935936\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-05-23 09:43:31\", \"id\": \"1150638318229858688\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-06-11 07:04:10\", \"id\": \"2236191955464452480\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-05-23 09:41:37\", \"id\": \"1150636396550760832\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-06-06 20:47:58\", \"id\": \"2229773463878052224\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-06-10 05:58:54\", \"id\": \"2234676696471701888\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-05-23 09:42:48\", \"id\": \"1150637585048709504\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-06-07 22:16:58\", \"id\": \"2231312595607587200\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-06-05 06:51:08\", \"id\": \"2227481525715970432\"}, {\"checkout_date\": \"\", \"create_time\": \"2021-07-30 12:07:43\", \"id\": \"1249352976427514240\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-10-30 21:58:55\", \"id\": \"2441479387729267072\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-05-30 21:08:29\", \"id\": \"2219647246059403648\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-06-11 07:05:57\", \"id\": \"2236193751012383104\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-06-11 07:06:06\", \"id\": \"2236193898400295296\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-06-07 22:16:54\", \"id\": \"2231312531854162304\"}, {\"checkout_date\": \"\", \"create_time\": \"2023-10-22 20:39:30\", \"id\": \"2429803039189354880\"}]\n");
        String jsonString_2="";
        //"[{\"checkout_date\": \"2023-09-19 00:00:00\", \"create_time\": \"2023-09-18 22:04:04\", \"id\": \"185181026\"}, {\"checkout_date\": \"2023-11-10 00:00:00\", \"create_time\": \"2023-11-08 08:11:47\", \"id\": \"188502574\"}, {\"checkout_date\": \"2021-07-31 00:00:00\", \"create_time\": \"2021-07-30 12:17:53\", \"id\": \"151109780\"}, {\"checkout_date\": \"2023-10-28 00:00:00\", \"create_time\": \"2023-10-27 15:16:55\", \"id\": \"187913969\"}, {\"checkout_date\": \"2023-06-05 00:00:00\", \"create_time\": \"2023-06-04 16:56:37\", \"id\": \"174754262\"}, {\"checkout_date\": \"2018-09-11 00:00:00\", \"create_time\": \"2018-09-10 20:16:10\", \"id\": \"105499858\"}, {\"checkout_date\": \"2018-06-10 00:00:00\", \"create_time\": \"2018-06-09 19:40:33\", \"id\": \"102185134\"}, {\"checkout_date\": \"2023-10-31 00:00:00\", \"create_time\": \"2023-10-30 21:46:27\", \"id\": \"188086465\"}, {\"checkout_date\": \"2021-07-31 00:00:00\", \"create_time\": \"2021-07-30 12:14:33\", \"id\": \"151109719\"}, {\"checkout_date\": \"2018-08-04 00:00:00\", \"create_time\": \"2018-08-02 05:20:08\", \"id\": \"103956440\"}, {\"checkout_date\": \"2019-07-27 00:00:00\", \"create_time\": \"2019-07-26 05:58:42\", \"id\": \"120647407\"}, {\"checkout_date\": \"2023-11-01 00:00:00\", \"create_time\": \"2023-10-31 19:53:24\", \"id\": \"188129447\"}, {\"checkout_date\": \"2019-06-06 00:00:00\", \"create_time\": \"2019-06-05 21:03:51\", \"id\": \"117364541\"}, {\"checkout_date\": \"2018-06-24 00:00:00\", \"create_time\": \"2018-06-23 20:40:20\", \"id\": \"102564295\"}, {\"checkout_date\": \"2018-05-23 00:00:00\", \"create_time\": \"2018-05-22 06:58:16\", \"id\": \"101775411\"}, {\"checkout_date\": \"2023-10-31 00:00:00\", \"create_time\": \"2023-10-30 21:53:18\", \"id\": \"188087824\"}, {\"checkout_date\": \"2023-11-10 00:00:00\", \"create_time\": \"2023-11-07 16:21:58\", \"id\": \"188475030\"}, {\"checkout_date\": \"2023-06-11 00:00:00\", \"create_time\": \"2023-06-06 20:53:11\", \"id\": \"174872691\"}, {\"checkout_date\": \"2018-07-02 00:00:00\", \"create_time\": \"2018-06-29 05:42:11\", \"id\": \"102704351\"}, {\"checkout_date\": \"2019-05-04 00:00:00\", \"create_time\": \"2019-04-28 19:32:23\", \"id\": \"113185415\"}, {\"checkout_date\": \"2021-04-30 00:00:00\", \"create_time\": \"2021-04-29 19:22:29\", \"id\": \"146168487\"}, {\"checkout_date\": \"2023-10-23 00:00:00\", \"create_time\": \"2023-10-22 20:32:53\", \"id\": \"187668660\"}, {\"checkout_date\": \"2021-04-23 00:00:00\", \"create_time\": \"2021-04-22 18:30:09\", \"id\": \"145719226\"}, {\"checkout_date\": \"2018-08-28 00:00:00\", \"create_time\": \"2018-08-27 20:41:29\", \"id\": \"105080253\"}, {\"checkout_date\": \"2023-06-11 00:00:00\", \"create_time\": \"2023-06-10 06:03:57\", \"id\": \"175070850\"}, {\"checkout_date\": \"2021-05-08 00:00:00\", \"create_time\": \"2021-05-07 06:31:17\", \"id\": \"146632948\"}, {\"checkout_date\": \"2019-04-21 00:00:00\", \"create_time\": \"2019-04-20 21:03:13\", \"id\": \"112808432\"}, {\"checkout_date\": \"2019-03-14 00:00:00\", \"create_time\": \"2019-03-13 18:31:15\", \"id\": \"111160976\"}]\n";
        System.out.println(new MessageGrouping().evaluate(jsonString_1.toString(), jsonString_2));
    }
}


