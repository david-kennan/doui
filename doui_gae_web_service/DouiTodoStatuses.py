'''
Created on May 29, 2013

@author: sergey
'''

'''local import'''
from doui_model import DouiSyncEntity
from datetime import datetime

'''GAE import'''
from google.appengine.ext import db

class DouiTodoStatus(DouiSyncEntity):
    """Datastorage entity for Doui todo status"""
    name = db.StringProperty()
    lastUpdateTimestamp = db.DateTimeProperty()
    
    JSON_STATUS_KEY = "updateObjectKey"
    JSON_STATUS_UPDATE_TIMESTAMP = "lastUpdateTimestamp"
    JSON_STATUS_NAME = "name"
    
    def createStatus(self, data):
        model = self.all()
        model.filter("name = ", data[self.JSON_STATUS_NAME] )
        model.filter("userId = ", self.userId)
        entity = model.fetch(1)
        if(len(entity) > 0):
            data[self.JSON_STATUS_KEY] = str(entity[0].key())
            self.updateStatus(data)
        else:
            self.loadAttrFromDict(data)
            data[self.JSON_STATUS_KEY] = str(db.put(self))
    
    def updateStatus(self, data):
        KeyForUpdate = db.get(data[self.JSON_STATUS_KEY])
        if(KeyForUpdate.lastUpdateTimestamp < datetime.strptime(data[self.JSON_STATUS_UPDATE_TIMESTAMP], "%Y-%m-%d %H:%M:%S")):
            KeyForUpdate.name = data[self.JSON_STATUS_NAME]
            KeyForUpdate.put() 
        else:
            data[self.JSON_STATUS_NAME] = KeyForUpdate.name
