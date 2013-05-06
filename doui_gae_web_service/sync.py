"""This module contains routines perform sync via http protocol"""
import doui_model
import webapp2

class Sync(webapp2.RequestHandler):
    def get(self):
        self.response.out.write("test")
