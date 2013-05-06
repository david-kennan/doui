""" This module contains routines for Doui data model"""
from google.appengine.ext import db

class DouiTodoItem(db.Model):
    title = db.StringProperty(required = True)
    body = db.TextProperty()
    updateTimestamp = db.DateTimeProperty()
    user = db.UserProperty()
    userId = db.StringProperty(required = True)
