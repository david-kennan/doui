"""This module contains routines perform sync via http protocol"""
import logging
import json
import webapp2

import doui_model
from google.appengine.ext import db
from datetime import datetime


class Sync(webapp2.RequestHandler):

    SYNC_OBJECTS_DICT = {"DouiTodoItem":doui_model.DouiTodoItem,
                         "DouiTodoCategories": doui_model.DouiTodoCategories,
                         "DouiTodoStatus": doui_model.DouiTodoStatuses}

    JSON_REQUEST_PARAM_NAME = "jsonData"
    
    JSON_UPDATED_OBJECT_VALUES = "updateObjectValues"

    JSON_UPDATED_OBJECT_KEY = "updateObjectKey"

    JSON_UPDATED_OBJECT_TIME = "updateObjectTime"

    JSON_UPDATED_OBJECT_TYPE = "updateObjectType"
    
    JSON_LAST_UPDATE_TIMESTAMP = "lastUpdateTimestamp"
    
    JSON_UPDATED_OBJECTS = "updatedObjects"
    
    JSON_UPDATE_OBJECT_CLIENT_ID = "client_id"
    
    def get(self):
        self.response.out.write(self.proceedRequest(self.request))
    
    def post(self):
        self.response.out.write(self.proceedRequest(self.request))
        
    def proceedRequest(self, request):
        """This method used to proceed request for update. Obtained HTTP request must contain JSON with data to be updated.
        Received objects will be sync with server database.
        This method will send back a JSON with objects to be updated on the client side. """
        strJsonData = request.get(Sync.JSON_REQUEST_PARAM_NAME)
        logging.info("Received JSON string: " + strJsonData)
        if((None != strJsonData) and (strJsonData != '')):
            requestObject = json.loads(strJsonData)
            result = self.proceedRequestObject(requestObject)  
        else:
            values = {}
            jsonAnswer = {}
            jsonAnswer[Sync.JSON_UPDATED_OBJECTS] = []
            for objectType in Sync.SYNC_OBJECTS_DICT.keys():
                values[Sync.JSON_UPDATED_OBJECT_VALUES] = []
                values[Sync.JSON_UPDATED_OBJECT_TYPE] = objectType 
                serverObjects = self.getServerObjectsAfterLastUpdate("2000-01-01 00:00:00", objectType)
                for objectValue in serverObjects.values(): 
                    values[Sync.JSON_UPDATED_OBJECT_VALUES].append(objectValue)
                jsonAnswer[Sync.JSON_UPDATED_OBJECTS].append(values.copy())
                result = json.dumps(jsonAnswer, cls = doui_model.jsonEncoder)
        return result

    def proceedRequestObject(self, requestObject):
        logging.debug("proceedRequestObject( requestObject )")
        lastUpdateTimestamp = requestObject[Sync.JSON_LAST_UPDATE_TIMESTAMP]
        updateObject = requestObject[Sync.JSON_UPDATED_OBJECTS][0]
        serverObjects = self.getServerObjectsAfterLastUpdate(lastUpdateTimestamp, updateObject[Sync.JSON_UPDATED_OBJECT_TYPE])
        for updateValues in updateObject[Sync.JSON_UPDATED_OBJECT_VALUES]: 
            if (None == updateValues[Sync.JSON_UPDATED_OBJECT_KEY]):
                objectModel = Sync.SYNC_OBJECTS_DICT[updateObject[Sync.JSON_UPDATED_OBJECT_TYPE]]
                dbObject = objectModel()
                dbObject.loadAttrFromDict(updateValues)
                # TODO: compare it with datastore object if it is exists.
                updateValues[Sync.JSON_UPDATED_OBJECT_KEY] = str(db.put(dbObject))
            else:
                KeyForUpdate = db.get(updateValues[Sync.JSON_UPDATED_OBJECT_KEY])
                if (updateObject[Sync.JSON_UPDATED_OBJECT_TYPE] == "DouiTodoItem"):
                    KeyForUpdate.title = updateValues["title"]
                    KeyForUpdate.body = updateValues["body"]
                    KeyForUpdate.fk_category = updateValues["fk_category"]
                    KeyForUpdate.fk_status = updateValues["fk_status"]
                else:
                    KeyForUpdate.name = updateValues["name"]
                KeyForUpdate.put()

        values = {}
        values[Sync.JSON_UPDATED_OBJECT_VALUES] = []
        values[Sync.JSON_UPDATED_OBJECT_TYPE] = updateObject[Sync.JSON_UPDATED_OBJECT_TYPE]
        for objectValue in serverObjects.values():
            values[Sync.JSON_UPDATED_OBJECT_VALUES].append(objectValue)
        requestObject[Sync.JSON_UPDATED_OBJECTS].append(values.copy())
        logging.info(json.dumps(requestObject, cls = doui_model.jsonEncoder))
        return json.dumps(requestObject, cls = doui_model.jsonEncoder)
    
    def getServerObjectsAfterLastUpdate(self, lastUpdateTimestamp, TypeOfObjects):
        """This method return a dictionary of objects which was updated after last device update time"""
        logging.debug("getServerObjectsAfterLastUpdate( lastUpdateTimestamp )")
        result = {};
        objectModelQuery = Sync.SYNC_OBJECTS_DICT[TypeOfObjects].all()
        objectModelQuery.filter("updateTimestamp > ", datetime.strptime(lastUpdateTimestamp, "%Y-%m-%d %H:%M:%S"))
        """objectModelQuery.filter("userId = ", users.get_current_user().user_id())"""
        for datastoreObject in objectModelQuery.run():
            result[datastoreObject.key().id_or_name()] = db.to_dict(datastoreObject)
            result[datastoreObject.key().id_or_name()][Sync.JSON_UPDATED_OBJECT_KEY] = str(datastoreObject.key())
            result[datastoreObject.key().id_or_name()][Sync.JSON_UPDATE_OBJECT_CLIENT_ID] = "null"
        return result

        
        
        
        
        
        
        
        
