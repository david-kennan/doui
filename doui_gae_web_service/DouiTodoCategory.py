'''
Created on May 29, 2013

@author: sergey
'''

'''local import'''
from doui_model import DouiSyncEntity
from datetime import datetime

'''GAE import'''
from google.appengine.ext import db

class DouiTodoCategory(DouiSyncEntity):
    """Datastorage entity for Doui todo categories"""
    name = db.StringProperty()
    is_deleted = db.StringProperty()
    lastUpdateTimestamp = db.DateTimeProperty()

    JSON_CATEGORY_KEY = "updateObjectKey"
    JSON_CATEGORY_UPDATE_TIMESTAMP = "lastUpdateTimestamp"
    JSON_CATEGORY_NAME = "name"
    JSON_CATEGORY_IS_DELETED = "is_deleted"

    def createCategory(self, data):
        model = self.all()
        model.filter("name = ", data[self.JSON_CATEGORY_NAME] )
        model.filter("userId = ", self.userId)
        entity = model.fetch(1)
        if(len(entity) > 0):
            data[self.JSON_CATEGORY_KEY] = str(entity[0].key())
            self.updateCategory(data)
        else:
            self.loadAttrFromDict(data)
            data[self.JSON_CATEGORY_KEY] = str(db.put(self))
        return (len(entity) > 0)
        
    def updateCategory(self, data):
        KeyForUpdate = db.get(data[self.JSON_CATEGORY_KEY])
        if(KeyForUpdate.lastUpdateTimestamp < datetime.strptime(data[self.JSON_CATEGORY_UPDATE_TIMESTAMP], "%Y-%m-%d %H:%M:%S")):
            KeyForUpdate.name = data[self.JSON_CATEGORY_NAME]
            KeyForUpdate.is_deleted = data[self.JSON_CATEGORY_IS_DELETED]
            KeyForUpdate.put() 
        else:
            data[self.JSON_CATEGORY_NAME] = KeyForUpdate.name
            data[self.JSON_CATEGORY_IS_DELETED] = KeyForUpdate.is_deleted
        