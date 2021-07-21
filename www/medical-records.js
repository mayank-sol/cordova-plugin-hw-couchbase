cordova.define("cordova-plugin-hw-couchbase.MedicalRecords", function(require, exports, module) {
    var exec = require('cordova/exec');

    var medicalRecords = {
        queryMedicalRecords: function(successCallback, errorCallback) {
            exec(successCallback, errorCallback, 'MedicalRecords', 'queryMedicalRecords');
        },
        getRecord: function(successCallback, errorCallback, args) {
            exec(successCallback, errorCallback, 'MedicalRecords', 'getRecord', args);
        },
        addRecord: function(successCallback, errorCallback, args) {
            exec(successCallback, errorCallback, 'MedicalRecords', 'addRecord', args);
        },
    };

    module.exports = medicalRecords;

});
