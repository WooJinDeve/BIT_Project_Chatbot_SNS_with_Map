from bs4 import BeautifulSoup
import re
import time
import requests

def Raesturant(text):
    string = "https://search.naver.com/search.naver?query={0}".format(text)
    html = requests.get(string)
    soup = BeautifulSoup(html.text, 'html.parser')

    titles = []
    images=[]
    ratings = []
    for i in range(0,6):
        title = soup.select('._1sfuL')[i].get_text()
        title = title.split('네이버')
        title = title[0].split('배달')

        titles.append(title[0])

        try:
            rating = soup.select('._17H46 > span > em')[i].text
            ratings.append(rating)
        except:
            ratings.append("-")


        image = soup.select('.zZGuI')[i].select('div > a > div')[0].find('div')
        image = image.attrs['style']

        image = image.split('"')
        images.append(image[1])


    return titles,ratings,images

def RestauranttoJson(text):
    titles, ratings, images = Raesturant(text)

    restaurants = {'restaurants': [
        {
            "title": titles[0],
            "rating": ratings[0],
            "image": images[0]
        },
        {
            "title": titles[1],
            "rating": ratings[1],
            "image": images[1]
        },
        {
            "title": titles[2],
            "rating": ratings[2],
            "image": images[2]
        },
        {
            "title": titles[3],
            "rating": ratings[3],
            "image": images[3]
        },
        {
            "title": titles[4],
            "rating": ratings[4],
            "image": images[4]
        },
        {
            "title": titles[5],
            "rating": ratings[5],
            "image": images[5]
        }
    ]
    }

    return restaurants