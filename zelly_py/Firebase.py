from datetime import datetime
from datetime import timedelta

import firebase_admin
from firebase_admin import credentials
from firebase_admin import firestore

# Use the application default credentials
cred = credentials.Certificate('파이어베이스 앱키')
firebase_admin.initialize_app(cred, {
    'projectId': '파이어베이스 앱키',
})

#날짜 가져오기
def today():
    return datetime.today().strftime("%Y-%m-%d")

def today_week1():
    now = datetime.now() + timedelta(6)
    return now.strftime("%Y-%m-%d")


def getCalendar_Friend(me, friend):
    getdb = firestore.client()
    docs = getdb.collection('calendar').get()

    dict_list = []
    for doc in docs:
        if doc.to_dict().get('calendarMe') == me and doc.to_dict().get('calendarFriend') == friend or doc.to_dict().get(
                'calendarMe') == friend and doc.to_dict().get('calendarFriend') == me and doc.to_dict().get('calendarDate') >= str(today()):
            if doc.to_dict().get('calendarDate') >= str(today()):
                dict_list.append(doc.to_dict())
    return dict_list

def getCalendar_Friend1(me, friend):
    getdb = firestore.client()
    docs = getdb.collection('calendar').get()
    dict_list = []
    for doc in docs:
        if doc.to_dict().get('calendarMe') == me or doc.to_dict().get('calendarMe') == friend:
            if today() <= doc.to_dict().get('calendarDate') <= today_week1():
                dict_list.append(doc.to_dict())

    return dict_list


def getCalendar_Schedule(me):
    print(me)
    getdb = firestore.client()
    list=[]

    docs = getdb.collection('users').document(me).collection('schedule').stream()
    for doc in docs:
        if doc.to_dict().get('state'):
            list.append(doc.to_dict())
    return list


def getCalendar(me):
    getdb = firestore.client()
    docs = getdb.collection('calendar').get()
    print(docs)
    dict_list = []
    for doc in docs:
        if doc.to_dict().get('calendarMe') == me or doc.to_dict().get('calendarFriend') == me:
            if doc.to_dict().get('calendarDate') >= str(today()):
                dict_list.append(doc.to_dict())
                print(doc.to_dict())

    return dict_list


def put_Schedule(me, friend, date, lat, lon, content):
    getdb = firestore.client()
    try:
        docs = getdb.collection('calendar').add({
            'calendarMe': me,
            'calendarFriend': friend,
            'calendarDate': date,
            'calendarLat': float(lat),
            'calendarLon': float(lon),
            'calendarContent': content,
        })
        return True
    except:
        return False