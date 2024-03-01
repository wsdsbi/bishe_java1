

let currentDate = new Date();
var today = currentDate.getFullYear() + '-' +
    ('0' + (currentDate.getMonth() + 1)).slice(-2) + '-' +
    ('0' + (currentDate.getDate())).slice(-2).toString()
console.log(today);