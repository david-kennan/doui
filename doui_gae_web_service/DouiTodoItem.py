'''
Created on May 29, 2013

@author: sergey
'''

'''local import'''
from doui_model import DouiSyncEntity
from datetime import datetime

'''GAE import'''
from google.appengine.ext import db
from google.appengine.ext.db import Key

class DouiTodoItem(DouiSyncEntity):
    """Datastorage entity for Doui todo item"""
    title = db.StringProperty()
    body = db.TextProperty()
    fk_category = db.StringProperty()
    fk_status = db.StringProperty()
    lastUpdateTimestamp = db.DateTimeProperty()
    
    JSON_ITEM_KEY = "updateObjectKey"
    JSON_ITEM_UPDATE_TIMESTAMP = "lastUpdateTimestamp"
    JSON_ITEM_TITLE = "title"
    JSON_ITEM_BODY = "body"
    JSON_ITEM_CATEGORY = "fk_category"
    JSON_ITEM_STATUS = "fk_status"

    def createItem(self, data):
        KeyForUpdate = db.get(data[self.JSON_ITEM_KEY])
        if(KeyForUpdate != None):
            self.updateItem(KeyForUpdate, data)
        else:
            self._key = Key(data[self.JSON_ITEM_KEY])
            self.loadAttrFromDict(data)
            self.put()
        
    def updateItem(self, KeyForUpdate, data):
        if(KeyForUpdate.lastUpdateTimestamp < datetime.strptime(data[self.JSON_ITEM_UPDATE_TIMESTAMP], "%Y-%m-%d %H:%M:%S")):
            KeyForUpdate.title = data[self.JSON_ITEM_TITLE]
            KeyForUpdate.body = data[self.JSON_ITEM_BODY]
            KeyForUpdate.fk_category = data[self.JSON_ITEM_CATEGORY]
            KeyForUpdate.fk_status = data[self.JSON_ITEM_STATUS]
            KeyForUpdate.put()
        else:
            data[self.JSON_ITEM_TITLE] = KeyForUpdate.title
            data[self.JSON_ITEM_BODY] = KeyForUpdate.body
            data[self.JSON_ITEM_CATEGORY] = KeyForUpdate.fk_category 
            data[self.JSON_ITEM_STATUS] = KeyForUpdate.fk_status 
            KeyForUpdate.put()
            
    def generateKeys(self, data):
        data["itemsKeys"] = []
        
        baseKey = db.Key.from_path('DouiTodoCategory', 1)
        ids = db.allocate_ids(baseKey, data["itemsCount"])
        idsRange = range(ids[0], ids[1] + 1)
        
        for item in range(0, data["itemsCount"]):
            data["itemsKeys"].append( str(db.Key.from_path('DouiTodoCategory', idsRange[item])))
            