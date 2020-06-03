from app import app
from services import classification_prediction


@app.route('/hello')
def hello():
    return classification_prediction('Hello, World, John!')
