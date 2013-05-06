""" This module intended to provide Web GUI to access data from the Doui 
application cloud storage"""
import jinja2
import os
import doui_model
import webapp2
import sync

from google.appengine.api import users

jinja_environment = jinja2.Environment(
    loader = jinja2.FileSystemLoader(os.path.dirname(__file__)),
    extensions = ['jinja2.ext.autoescape'])

class WebGui(webapp2.RequestHandler):
    def get(self):
    
        doui_query = doui_model.DouiTodoItem.all()
        doui_items = doui_query.fetch(10)
  
        template = jinja_environment.get_template('index.html')
        self.response.out.write(template.render(doui_items = doui_items))

application = webapp2.WSGIApplication([
    ('/', WebGui),
    ('/sync', sync.Sync),
], debug = True)
