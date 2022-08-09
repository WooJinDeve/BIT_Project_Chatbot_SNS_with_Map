from bs4 import BeautifulSoup
import requests
import re
import time


def Wheather(text):
    string = "https://search.naver.com/search.naver?query={0}".format(text)
    html = requests.get(string)

    soup = BeautifulSoup(html.text, 'html.parser')

    # 현재 위치, 기온 찾기
    location = soup.select('h2')[0].text

    temperature = soup.select('.temperature_text')[0].get_text()
    temperature = [float(s) for s in re.findall(r'-?\d+\.?\d*', temperature)]  # 기온 찾기
    temperature = temperature[0]
    # 미세먼지, 초미세먼지, 오존
    dust_temp = soup.select('.today_chart_list')[1].get_text()
    dust_temp = dust_temp.strip()
    dust = dust_temp.split(' ')
    weather_temp = soup.select('.temperature_info')[0].get_text()
    weather_temp = weather_temp.strip()
    weather_value = weather_temp.split(' ')

    return {"location": location,
            "temperature": temperature,
            "dust": dust[0],
            "dust_value": dust[1],
            "weather_value": weather_value[4],
            "humidity": weather_value[10]}