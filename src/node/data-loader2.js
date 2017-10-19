var MongoClient = require('mongodb').MongoClient;

var dbConnection = null;

var lockCount = 0;



function getDbConnection(callback){
    MongoClient.connect("mongodb://localhost/liferecord", function(err, db){
        if(err){
            console.log("Unable to connect to Mongodb");
        }else{
            dbConnection = db;
            callback();
        }
    });
};

function closeConnection() {
    if (dbConnection)
        dbConnection.close();

}

getDbConnection(function(){
    dbConnection.dropDatabase(function(err,doc){
        if (err)
            console.log("Could not drop database");
        else
            addRecord();
    });
});

function addRecord() {
    d = [{
        "recordName":    "Today good",
        "recordInfo":     "Today is a good day.",
        "albumId":     "11223344",
        "targetId":    "123",
        "viewId":     "555",
        "likeId":    "666",
        "editorId":     "4323"
    },
        {
            "recordName":    "Today bad",
            "recordInfo":     "Today is a bad day.",
            "albumId":     "55667788",
            "targetId":    "456",
            "viewId":     "777",
            "likeId":    "888",
            "editorId":     "1122"
        }];
    var records = dbConnection.collection('records');
    records.insertOne(d[0], function(err,doc){
        if (err){
            console.log("Could not add record 1");
        }
        else {
            addPicstoRecord(doc.ops[0]._id.toString(),120);
        }
    })
    records.insertOne(d[1], function(err,doc){
        if (err){
            console.log("Could not add record 1");
        }
        else {
            addPicstoRecord(doc.ops[0]._id.toString(),140);
        }
    })
}

urlList = ['http://abc.com/a','http://cde.com/c','http://efg.com/e','http://hij.com/h',
    'http://klm.com/k','http://nop.com/n','http://qrs.com/q','http://tuv.com/t','http://wxyz.com/w'];

function addPicstoRecord(recordId,count) {

    sequence = Array(count);
    console.log("sequence",sequence);
    var c = [];
    for (i=0;i<count;i++){
        console.log("Trying")
        var url = urlList[Math.floor(Math.random() * urlList.length)]+i+'.png';
        var recordId = recordId;

        c.push ({
            url: url,
            recordId: recordId
        });

    }
    c.forEach(function(picture){
        var pictures = dbConnection.collection('pictures');
        pictures.insertOne(picture);
    })

}

function addPicstoRecord1(recordId) {
    c = [{
        "url" : "http://qqq.com/p1.png",
        "recordId" : recordId
    },{
        "url" : "http://qqq.com/p2.png",
        "recordId" : recordId
    }];
    c.forEach(function(picture){
        var pictures = dbConnection.collection('pictures');
        pictures.insertOne(picture);
    })

}

setTimeout(closeConnection,5000);