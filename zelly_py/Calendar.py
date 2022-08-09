import Firebase
from datetime import datetime
from datetime import timedelta

def schedulestoJson(notJsonSchedules):
    str_list = ', '.join(map(str, notJsonSchedules))
    str_list = '{\'schedules\': [' + str_list + '], "state":"schedules"}'


    print(str_list)
    return str_list

def ChatbotCalendarJson(listdate, listtime):
    str_list = ', '.join(map(str, listdate))
    str_listtime = ', '.join(map(str, listtime))
    return '{\'ChatbotCalendar\': [' + str_list + '], "time" : ['+str_listtime+'], "state":"ChatbotCalendar"}'

def ChatbotCalendar(me, friend):
    user = Firebase.getCalendar_Schedule(me)
    friend = Firebase.getCalendar_Schedule(friend)
    mixschedule =  Firebase.getCalendar_Friend1(me,friend)

    userlist = []
    usertime = []
    userfriendlist = []
    userfriendtime = []
    schedulelist = []
    scheduletime = []

    for doc in user:
        userlist.append(doc.get('schedule'))
        usertime.append(doc.get("timestamp"))

    for doc in friend:
        userfriendlist.append(doc.get('schedule'))
        userfriendtime.append(doc.get("timestamp"))

    for doc in mixschedule:
        schedulelist.append(doc.get('calendarDate'))
        scheduletime.append(doc.get('timestamp'))


    datenum = datetime.today().weekday()
    date = datetime.today()

    timelist = []
    for i in range(datenum, 7):
        timelist.append(date.strftime("%Y-%m-%d"))
        date = date + timedelta(1)

    for i in range(0, datenum):
        timelist.insert(i, date.strftime("%Y-%m-%d"))
        date = date + timedelta(1)

    time = []
    for i in range(0,len(userlist)):
        for j in range(0, 7):
            dummy = usertime[i].split(':')
            starthour = dummy[0]
            endhour = int(dummy[2])+1
            if endhour == 24:
                endhour = '00'
            if userlist[i][j] == '1':
                if len(time) < 7:
                    time.append(starthour + ":00:" + str(endhour) + ":00")
                else:
                    if time[i] == '':
                        st = starthour + ":00:" + str(endhour) + ":00"
                    else:
                        st = time[i] + '/' + starthour + ":00:" + str(endhour) + ":00"
                    time[i] = st
            else:
                if len(time) < 7:
                    time.append('')

    for i in range(0, len(userfriendlist)):
        for j in range(0, 7):
            dummy = userfriendtime[i].split(':')
            starthour = dummy[0]
            endhour = int(dummy[2]) + 1
            if endhour == 24:
                endhour = '00'
            if userfriendlist[i][j] == '1':
                if len(time) < 7:
                    time.append(starthour + ":00:" + str(endhour) + ":00")
                else:
                    if time[i] == '':
                        st = starthour + ":00:" + str(endhour) + ":00"
                    else:
                        st = time[i] + '/' + starthour + ":00:" + str(endhour) + ":00"
                    time[i] = st
            else:
                if len(time) < 7:
                    time.append('')

    for i in range(0,7):
        for j in range(0,len(scheduletime)):
            if timelist[i] == schedulelist[j]:
                dummy = scheduletime[j].split(':')
                starthour = dummy[0]
                endhour = int(dummy[2]) + 1
                if endhour == 24:
                    endhour = '00'

                if len(time) < 7:
                    time.append(starthour + ":00:"+str(endhour)+":00")
                else:
                    if time[i] == '':
                        st = starthour + ":00:" + str(endhour) + ":00"
                    else:
                        st = time[i] + '/' + starthour + ":00:" + str(endhour) + ":00"
                    time[i] = st
            else:
                if len(time) < 7:
                    time.append('')

    print(time)

    if len(time) == 0:
        return '{\'ChatbotCalendar\': [{"result": "null"}], "state":"ChatbotCalendar"}'

    return ChatbotCalendarJson(timelist, time)




# 한명의 캘린더
def userCalendar(me, date):
    # date =datetime.datetime.strftime(date, '%Y-%m-%d')
    docs = Firebase.getCalendar(me)
    print(docs)
    schedules = []

    for doc in docs:
        d = doc.get('calendarDate')
        # d = datetime.datetime.strftime(d, '%Y-%m-%d')
        if d == date:
            doc['calendarDate'] = d
            schedules.append(doc)

    if len(schedules) == 0:
        return {"value": "일정이 없습니다.", "state": "텍스트"}

    return schedulestoJson(schedules)


# 한명의 캘린더/날짜 없이
def userCalendar_NoDate(me):
    # date =datetime.datetime.strftime(date, '%Y-%m-%d')
    docs = Firebase.getCalendar(me)

    schedules = []

    for doc in docs:
        d = doc.get('calendarDate')
        # d = datetime.datetime.strftime(d, '%Y-%m-%d')
        doc['calendarDate'] = d
        schedules.append(doc)

    if len(schedules) == 0:
        return {"value": "일정이 없습니다.", "state":"텍스트"}

    return schedulestoJson(schedules)


# 두명의 캘린더
def userCalendar_Friend(me, friend, date):
    # date = datetime.datetime.strftime(date, '%Y-%m-%d')
    docs = Firebase.getCalendar_Friend(me, friend)
    print(docs)
    schedules = []

    for doc in docs:
        d = doc.get('calendarDate')
        # d = datetime.datetime.strftime(d, '%Y-%m-%d')
        if d == date:
            doc['calendarDate'] = d
            schedules.append(doc)

    if len(schedules) == 0:
        return {"value": "일정이 없습니다.", "state":"텍스트"}

    return schedulestoJson(schedules)


# 두명의 캘린더/날짜 없을 시
def userCalendar_Friend_NoDate(me, friend):
    # date = datetime.datetime.strftime(date, '%Y-%m-%d')
    docs = Firebase.getCalendar_Friend(me, friend)

    schedules = []

    for doc in docs:
        d = doc.get('calendarDate')
        # d = datetime.datetime.strftime(d, '%Y-%m-%d')
        doc['calendarDate'] = d
        schedules.append(doc)

    if len(schedules) == 0:
        return {"value": "일정이 없습니다.", "state":"텍스트"}


    return schedulestoJson(schedules)


# 약속 잡기
def makeSchedule(me, friend, date, lat, lon, content):
    # date = datetime.datetime.strftime(date, '%Y-%m-%d')
    value = Firebase.put_Schedule(me, friend, date, lat, lon, content)
    if value:

        return '{\'makeSchedules\': [{"result": "저장을 성공하였습니다."}], "state":"makeSchedules"}'
    else:
        return '{\'makeSchedules\': [{"result": "저장을 실패하였습니다."}], "state":"makeSchedules"}'
