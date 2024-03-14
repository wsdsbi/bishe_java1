package bishe.entity;

public class Metal_info {
    String type;
    String bargin;
    String height;
    String price;
    String desc;
    String owned;

    String createtime;
    String account;
    String date;
    String station;

    @Override
    public String toString() {
        return "Metal_info{" +
                "type='" + type + '\'' +
                ", bargin='" + bargin + '\'' +
                ", height='" + height + '\'' +
                ", price='" + price + '\'' +
                ", desc='" + desc + '\'' +
                ", owned='" + owned + '\'' +
                ", createtime='" + createtime + '\'' +
                ", account='" + account + '\'' +
                ", date='" + date + '\'' +
                ", station='" + station + '\'' +
                ", status=" + status +
                ", address='" + address + '\'' +
                ", myaccount='" + myaccount + '\'' +
                ", updatetime='" + updatetime + '\'' +
                '}';
    }

    public String getStation() {
        return station;
    }

    public void setStation(String station) {
        this.station = station;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    int status;
    String address;

    public String getMyaccount() {
        return myaccount;
    }

    public void setMyaccount(String myaccount) {
        this.myaccount = myaccount;
    }

    String myaccount;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public Metal_info(String type, String bargin, String height, String price, String desc, String owned, String createtime, String account, String updatetime) {
        this.type = type;
        this.bargin = bargin;
        this.height = height;
        this.price = price;
        this.desc = desc;
        this.owned = owned;
        this.createtime = createtime;
        this.account = account;
        this.updatetime = updatetime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBargin() {
        return bargin;
    }

    public void setBargin(String bargin) {
        this.bargin = bargin;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getOwned() {
        return owned;
    }

    public void setOwned(String owned) {
        this.owned = owned;
    }

    public String getCreatetime() {
        return createtime;
    }

    public void setCreatetime(String createtime) {
        this.createtime = createtime;
    }

    public String getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(String updatetime) {
        this.updatetime = updatetime;
    }

    public Metal_info() {
    }

    public Metal_info(String type, String bargin, String height, String price, String desc, String owned, String createtime, String updatetime) {
        this.type = type;
        this.bargin = bargin;
        this.height = height;
        this.price = price;
        this.desc = desc;
        this.owned = owned;
        this.createtime = createtime;
        this.updatetime = updatetime;
    }

    String updatetime;
}
