from flask import Flask, jsonify
from flask import request

from Calendar import *
from chatbot.chat import *
from place import *
from weather import *
from resturant import *
from Unicode import *  # 유니코드 -> 한글 변환 코드
import json

app = Flask(__name__)
app.config['JSON_AS_ASCII'] = False


# 주소 형식: http://localhost:5000/chatbot/일정잡아줘/?me=12&friend=null&date=2022-06-21&lat=32&lon=127&content=fds
@app.route('/chatbot/<text>/')
def Chatbot(text):
    data = unitokor(text)
    # 5000에서 실행할 때는 unitokor부분 지우고 data text로 바꾸기

    me = request.args.get('me', "null")
    friend = request.args.get('friend', "null")
    date = request.args.get('date', "null")
    lat = request.args.get('lat', "null")
    lon = request.args.get('lon', "null")
    content = request.args.get('content', "null")
    content = unitokor(content)
    result = chatbot(data)
    if result["state"] == "날씨":
        dict = Wheather(data)
        dict["state"] = result["state"]
        dict["value"] = result["value"]
        return dict

    elif result["state"] == "맛집":

        restaurants = RestauranttoJson(data)
        restaurants["state"] = result["state"]
        restaurants["value"] = result["value"]

        return restaurants


    elif result["state"] == "명소":

        places = PlacetoJson(data)
        places["state"] = result["state"]
        places["value"] = result["value"]

        return places


    elif result["state"] == "캘린더":
        if friend == 'null':
            if date == 'null':
                return userCalendar_NoDate(me)

            else:
                return userCalendar(me, date)

        else:
            if date == 'null':
                return userCalendar_Friend_NoDate(me, friend)

            else:
                return userCalendar_Friend(me, friend, date)

    elif result["state"] == "일정잡기":
        return ChatbotCalendar(me, friend)

    # elif result["state"] == "일정잡기":
    #
    #     if date == 'null' and lat == 'null' and lon == 'null' and content == 'null':
    #         return 'calendar_save'
    #
    #     value = makeSchedule(me, friend, date, lat, lon, content)
    #
    #     if value == True:
    #         return 'calendar_save_true'
    #     else:
    #         return 'calendar_save_false'

    return result



@app.route('/chatbot/update')
def Chatbot_Update():
    chatbot_update()
    return True


@app.route('/weather/<text>')
def Weather(text):
    text = unitokor(text)
    return Wheather(text)


@app.route('/resturant/<text>')
def Restaurant(text):
    return Raesturant(text)


if __name__ == "__main__":
    app.run(host='0.0.0.0')
