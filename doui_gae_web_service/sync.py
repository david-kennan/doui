"""This module contains routines perform sync via http protocol"""
import logging
import json
import webapp2

import doui_model
from google.appengine.ext import db
from google.appengine.api import users
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
    
    JSON_UPDATE_ITEM_FK_STATUS = "fk_status"
    JSON_UPDATE_ITEM_FK_CATEGORY = "fk_category"
    
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
            requestObject[Sync.JSON_LAST_UPDATE_TIMESTAMP] = "2000-01-01 00:00:00:00"
            requestObject[Sync.JSON_UPDATED_OBJECTS] = []
        return self.proceedRequestObject(requestObject)

    def proceedRequestObject(self, requestObject):
        logging.debug("proceedRequestObject( requestObject )")
        serverObjects = self.getServerObjectsAfterLastUpdate(requestObject[Sync.JSON_LAST_UPDATE_TIMESTAMP])
        
        items = self.updateStatuses(requestObject)
        for item in items:
            serverObjects["DouiTodoStatus"].append(item.copy())
        
        items = self.updateCategories(requestObject)
        for item in items:
            serverObjects["DouiTodoCategories"].append(item.copy())
        
        items = self.updateItems(requestObject)
        for item in items:
            serverObjects["DouiTodoItem"].append(item.copy())
        
        
        requestObject[Sync.JSON_UPDATED_OBJECTS] = []
             
        values = {}
        values[Sync.JSON_UPDATED_OBJECT_VALUES] = []
        for objectType in serverObjects.keys():
            values[Sync.JSON_UPDATED_OBJECT_VALUES] = []
            values[Sync.JSON_UPDATED_OBJECT_TYPE] = objectType
            for objectValue in serverObjects[objectType]:
                values[Sync.JSON_UPDATED_OBJECT_VALUES].append(objectValue)
            requestObject[Sync.JSON_UPDATED_OBJECTS].append(values.copy())
        logging.info(json.dumps(requestObject, cls = doui_model.jsonEncoder))
        requestObject[Sync.JSON_LAST_UPDATE_TIMESTAMP] = datetime.now().strftime("%Y-%m-%d %H:%M:%S:%f")
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
        result = []
        item = {}
        objectModelQuery = objectModel.all()
        objectModelQuery.filter("updateTimestamp > ", datetime.strptime(lastUpdateTimestamp, "%Y-%m-%d %H:%M:%S:%f"))
        objectModelQuery.filter("userId = ", users.get_current_user().user_id())
        for datastoreObject in objectModelQuery.run():
            item = db.to_dict(datastoreObject);
            item[Sync.JSON_UPDATED_OBJECT_KEY] = str(datastoreObject.key())
            item[Sync.JSON_UPDATE_OBJECT_CLIENT_ID] = "null"
            result.append(item.copy())
        return result
        
    def updateStatuses(self, requestObject):
        result = []
        valuesForUpdate = self.getObjectsByType(requestObject, "DouiTodoStatus")
        for value in valuesForUpdate:
            if(None == value[Sync.JSON_UPDATED_OBJECT_KEY]):
                entity = self.getObjectByName("DouiTodoStatus", value["name"])
                if(len(entity) > 0):
                    value[Sync.JSON_UPDATED_OBJECT_KEY] = str(entity[0].key())
                else:
                    dbObject = Sync.SYNC_OBJECTS_DICT["DouiTodoStatus"](user = users.get_current_user(),
                                                                        userId = users.get_current_user().user_id())
                    dbObject.loadAttrFromDict(value)
                    value[Sync.JSON_UPDATED_OBJECT_KEY] = str(db.put(dbObject))
                
                result.append(value)
                requestItems = self.getObjectsByType(requestObject, "DouiTodoItem")
                for item in requestItems:
                    if(item[Sync.JSON_UPDATE_ITEM_FK_STATUS] == value[Sync.JSON_UPDATE_OBJECT_CLIENT_ID]):
                        item[Sync.JSON_UPDATE_ITEM_FK_STATUS] = value[Sync.JSON_UPDATED_OBJECT_KEY]  
            else:
                KeyForUpdate = db.get(value[Sync.JSON_UPDATED_OBJECT_KEY])
                if(KeyForUpdate.lastUpdateTimestamp < datetime.strptime(value[Sync.JSON_LAST_UPDATE_TIMESTAMP], "%Y-%m-%d %H:%M:%S")):
                    KeyForUpdate.name = value["name"]
                    KeyForUpdate.put()
                
        requestObject[Sync.JSON_UPDATED_OBJECTS]
        return result
                
    def updateCategories(self, requestObject):
        result = []
        valuesForUpdate = self.getObjectsByType(requestObject, "DouiTodoCategories")
        for value in valuesForUpdate:
            if(None == value[Sync.JSON_UPDATED_OBJECT_KEY]):
                entity = self.getObjectByName("DouiTodoCategories", value["name"])
                if(len(entity) > 0):
                    value[Sync.JSON_UPDATED_OBJECT_KEY] = str(entity[0].key()) 
                    value["is_deleted"] = entity[0].is_deleted
                else:
                    dbObject = Sync.SYNC_OBJECTS_DICT["DouiTodoCategories"](user = users.get_current_user(),
                                                                            userId = users.get_current_user().user_id())
                    dbObject.loadAttrFromDict(value)
                    value[Sync.JSON_UPDATED_OBJECT_KEY] = str(db.put(dbObject))
                
                result.append(value)
                requestItems = self.getObjectsByType(requestObject, "DouiTodoItem")
                for item in requestItems:
                    if(item[Sync.JSON_UPDATE_ITEM_FK_CATEGORY] == value[Sync.JSON_UPDATE_OBJECT_CLIENT_ID]):
                        item[Sync.JSON_UPDATE_ITEM_FK_CATEGORY] = value[Sync.JSON_UPDATED_OBJECT_KEY]
            else:
                KeyForUpdate = db.get(value[Sync.JSON_UPDATED_OBJECT_KEY])
                if(KeyForUpdate.lastUpdateTimestamp < datetime.strptime(value[Sync.JSON_LAST_UPDATE_TIMESTAMP], "%Y-%m-%d %H:%M:%S")):
                    KeyForUpdate.name = value["name"]
                    KeyForUpdate.is_deleted = value["is_deleted"]
                    KeyForUpdate.put()
                    result.append(value)
        return result
        
    def updateItems(self, requestObject):
        result = []
        valuesForUpdate = self.getObjectsByType(requestObject, "DouiTodoItem")
        for value in valuesForUpdate:
            if (None == value[Sync.JSON_UPDATED_OBJECT_KEY]):
                dbObject = Sync.SYNC_OBJECTS_DICT["DouiTodoItem"](user = users.get_current_user(),
                                                                  userId = users.get_current_user().user_id())
                dbObject.loadAttrFromDict(value)
                value[Sync.JSON_UPDATED_OBJECT_KEY] = str(db.put(dbObject))
                result.append(value)
            else:
                KeyForUpdate = db.get(value[Sync.JSON_UPDATED_OBJECT_KEY])
                if(KeyForUpdate.lastUpdateTimestamp < datetime.strptime(value[Sync.JSON_LAST_UPDATE_TIMESTAMP], "%Y-%m-%d %H:%M:%S")):
                    KeyForUpdate.title = value["title"]
                    KeyForUpdate.body = value["body"]
                    KeyForUpdate.fk_category = value["fk_category"]
                    KeyForUpdate.fk_status = value["fk_status"]
                    KeyForUpdate.put()
        return result
        
    def getObjectsByType(self, requestObject, objectType):
        result = []
        for updateObject in requestObject[Sync.JSON_UPDATED_OBJECTS]:
            if(updateObject[Sync.JSON_UPDATED_OBJECT_TYPE] == objectType):
                result = updateObject[Sync.JSON_UPDATED_OBJECT_VALUES]
                break
        return result
    
    def getObjectByName(self, objectType, name):
        objectModel = Sync.SYNC_OBJECTS_DICT[objectType](user = users.get_current_user(),
                                                         userId = users.get_current_user().user_id()).all()
        objectModel.filter("name = ", name )
        objectModel.filter("userId = ", users.get_current_user().user_id())
        entity = objectModel.fetch(1)
        return entity 
        
        
