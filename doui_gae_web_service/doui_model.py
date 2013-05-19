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
                setattr(self, a, DouiSyncEntity(b) if isinstance(b, dict) else b)
        
class DouiTodoItem(DouiSyncEntity):
    """Datastorage entity for Doui todo item"""
    client_id = db.StringProperty()
    title = db.StringProperty()
    body = db.TextProperty()
    fk_category = db.StringProperty()
    fk_status = db.StringProperty()
    
class DouiTodoCategories(DouiSyncEntity):
    """Datastorage entity for Doui todo categories"""
    client_id = db.StringProperty()
    name = db.StringProperty()
    
class DouiTodoStatuses(DouiSyncEntity):
    """Datastorage entity for Doui todo categories"""
    client_id = db.StringProperty()
    name = db.StringProperty()

