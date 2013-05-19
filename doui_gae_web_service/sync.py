"""This module contains routines perform sync via http protocol"""
import logging
import json
import webapp2

import doui_model
from google.appengine.ext import db
import datetime


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
        else:
            requestObject = {}
            requestObject[Sync.JSON_LAST_UPDATE_TIMESTAMP] = datetime.datetime(2000, 01, 01, 0, 0)
            requestObject[Sync.JSON_UPDATED_OBJECTS] = []
        return self.proceedRequestObject(requestObject)

    def proceedRequestObject(self, requestObject):
        logging.debug("proceedRequestObject( requestObject )")
        lastUpdateTimestamp = requestObject[Sync.JSON_LAST_UPDATE_TIMESTAMP]
        serverObjects = self.getServerObjectsAfterLastUpdate(lastUpdateTimestamp)
        for updateObject in requestObject[Sync.JSON_UPDATED_OBJECTS]:
            if (None == updateObject[Sync.JSON_UPDATED_OBJECT_KEY]):
                objectModel = Sync.SYNC_OBJECTS_DICT[updateObject[Sync.JSON_UPDATED_OBJECT_TYPE]]
                dbObject = objectModel()
                dbObject.loadAttrFromDict(updateObject[Sync.JSON_UPDATED_OBJECT_VALUES])
                # TODO: compare it with datastore object if it is exists.
                updateObject[Sync.JSON_UPDATED_OBJECT_KEY] = str(db.put(dbObject))
            else:
                KeyForUpdate = db.get(updateObject[Sync.JSON_UPDATED_OBJECT_KEY])
                if (updateObject[Sync.JSON_UPDATED_OBJECT_TYPE] == "DouiTodoItem"):
                    KeyForUpdate.title = updateObject[Sync.JSON_UPDATED_OBJECT_VALUES]["title"]
                    KeyForUpdate.body = updateObject[Sync.JSON_UPDATED_OBJECT_VALUES]["body"]
                    KeyForUpdate.fk_category = updateObject[Sync.JSON_UPDATED_OBJECT_VALUES]["fk_category"]
                    KeyForUpdate.fk_status = updateObject[Sync.JSON_UPDATED_OBJECT_VALUES]["fk_status"]
                else:
                    KeyForUpdate.name = updateObject[Sync.JSON_UPDATED_OBJECT_VALUES]["name"]
                KeyForUpdate.put()

        for objectType in serverObjects.keys():
            for objectValue in serverObjects[objectType].values():
                requestObject[Sync.JSON_UPDATED_OBJECTS].append(objectValue);
        return json.dumps(requestObject, cls = doui_model.jsonEncoder)
    
    def getServerObjectsAfterLastUpdate(self, lastUpdateTimestamp):
        """This method return a dictionary of objects which was updated after last device update time"""
        logging.debug("getServerObjectsAfterLastUpdate( lastUpdateTimestamp )")
        result = {};
        for objectType in Sync.SYNC_OBJECTS_DICT.keys():
            result[objectType] = self.getServerObjectsAfterLastUpdateByType(lastUpdateTimestamp, Sync.SYNC_OBJECTS_DICT[objectType])
        return result
            
    def getServerObjectsAfterLastUpdateByType(self, lastUpdateTimestamp, objectModel):
        """ This method returns a dictionary with objects for concrete type, which was updated after last update"""
        result = {}
        objectModelQuery = objectModel.all()
        objectModelQuery.filter("updateTimestamp > ", lastUpdateTimestamp)
        """objectModelQuery.filter("userId = ", users.get_current_user().user_id())"""
        for datastoreObject in objectModelQuery.run(): 
            result[datastoreObject.key().id_or_name()] = db.to_dict(datastoreObject)
            result[datastoreObject.key().id_or_name()]["key"] = datastoreObject.key().id_or_name()
        return result
        
        
        
