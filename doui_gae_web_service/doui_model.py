""" This module contains routines for Doui data model"""
from google.appengine.ext import db
import json
import datetime

class jsonEncoder(json.JSONEncoder):
    def default(self, obj):
        if isinstance(obj, DouiSyncEntity):
            return "doui_model.DouiSyncEntity"
        elif isinstance(obj, db.DateTimeProperty):
            return "db.DateTimeProperty"
        
        elif isinstance(obj, datetime.datetime):
            return obj.isoformat()
        
        elif isinstance(obj, db.TextProperty):
            return unicode(obj)
        
        elif isinstance(obj, db.StringProperty):
            return unicode(obj)
        
        elif isinstance(obj, db.Model):
            return dict((p, getattr(obj, p)) 
                        for p in obj.properties())

        elif isinstance(obj, db.UserProperty):
            return obj.__dict__

        else:
            ""  # return json.JSONEncoder.default(self, obj)
        
class DouiSyncEntity(db.Model):
    """ Class which represent common parent for all entities stored to the Doui datastorage."""
    updateTimestamp = db.DateTimeProperty(auto_now = True)
    def loadAttrFromDict(self, dictAttrs):
        for a, b in dictAttrs.items():
            if isinstance(b, (list, tuple)):
                setattr(self, a, [DouiSyncEntity(x) if isinstance(x, dict) else x for x in b])
            else:
                if isinstance(b, dict):
                    value =  DouiSyncEntity(b)
                else:
                    if(a == "lastUpdateTimestamp"):
                        value = datetime.datetime.strptime(b, "%Y-%m-%d %H:%M:%S")
                    else:
                        value = b
                setattr(self, a, value)
        
class DouiTodoItem(DouiSyncEntity):
    """Datastorage entity for Doui todo item"""
    title = db.StringProperty()
    body = db.TextProperty()
    fk_category = db.StringProperty()
    fk_status = db.StringProperty()
    lastUpdateTimestamp = db.DateTimeProperty()
    
class DouiTodoCategories(DouiSyncEntity):
    """Datastorage entity for Doui todo categories"""
    name = db.StringProperty()
    is_deleted = db.StringProperty()
    lastUpdateTimestamp = db.DateTimeProperty()
    
class DouiTodoStatuses(DouiSyncEntity):
    """Datastorage entity for Doui todo categories"""
    name = db.StringProperty()
    lastUpdateTimestamp = db.DateTimeProperty()

