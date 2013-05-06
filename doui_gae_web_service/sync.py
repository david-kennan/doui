"""This module contains routines perform sync via http protocol"""
import logging
import json
import webapp2

import doui_model
from google.appengine.ext import db
from google.appengine.api import users

class Sync(webapp2.RequestHandler):

    SYNC_OBJECTS_DICT = {"DouiTodoItem":doui_model.DouiTodoItem}

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
        strJsonData = request.get(Sync.JSON_REQUEST_PARAM_NAME)
        logging.debug("Received JSON string: " + strJsonData)
        if((None != strJsonData) and (strJsonData != '')):
            requestObject = json.loads(strJsonData)
            return self.proceedRequestObject(requestObject)

    def proceedRequestObject(self, requestObject):
        requestObject[Sync.JSON_LAST_UPDATE_TIMESTAMP] = 12
        for updateObject in requestObject[Sync.JSON_UPDATED_OBJECTS]:
            if (None == updateObject[Sync.JSON_UPDATED_OBJECT_KEY]):
                if(updateObject[Sync.JSON_UPDATED_OBJECT_TYPE] == "DouiTodoItem"):
                    dbObject = doui_model.DouiTodoItem(
                                                       title = updateObject[Sync.JSON_UPDATED_OBJECT_VALUES]["title"],
                                                       body = updateObject[Sync.JSON_UPDATED_OBJECT_VALUES]["body"],
                                                       user = users.get_current_user(),
                                                       userId = users.get_current_user().user_id()
                                                       )
                    db.put(dbObject)
        return json.dumps(requestObject)
        
