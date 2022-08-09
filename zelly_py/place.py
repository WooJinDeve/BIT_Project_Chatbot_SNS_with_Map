from bs4 import BeautifulSoup
import requests
import re
import time

def Place(text):
    string = "https://search.daum.net/search?nil_suggest=btn&w=tot&DA=SBC&q={0}".format(text)
    html = requests.get(string)
    soup = BeautifulSoup(html.text, 'html.parser')

    titles = ['a', 'b', 'c', 'd', 'e']
    images = ['a', 'b', 'c', 'd', 'e']
    categories = ['a', 'b', 'c', 'd', 'e']
    addresses = ['a', 'b', 'c', 'd', 'e']


    for i in range(0, 5):
        title = soup.select('.info_item > span')[i].text
        # image = soup.select('.thumb')[i].find('img')
        # image = image.attrs['src']
        image = soup.select('.list_item > li')[i].find('img')
        image = image.attrs['src']

        titles[i] = title
        images[i] = image




    for i in range(0, 5):
        try:
            string = "https://search.naver.com/search.naver?where=nexearch&sm=top_hty&fbm=1&ie=utf8&query=" + titles[i]
            html = requests.get(string)
            soup = BeautifulSoup(html.text, 'html.parser')

            category = soup.select('._1_hm2 > a > span')[1].text
            categories[i] = category

            address = soup.select('._1h3B_ > a > span')[0].text
            addresses[i] = address
        except:
            try:
                category = soup.select('._2s4DU > span')[1].text
                categories[i] = category

                address = soup.select('._1B9G6 > div > span > a')[0].text
                addresses[i] = address
            except:
                images[i] = 0
                titles[i] = 0
                addresses[i] = 0
                categories[i] = 0

    print(titles)
    print(categories)
    print(addresses)
    print(images)

    return titles, categories, addresses, images





def PlacetoJson(text):
    titles, category, addresses, images = Place(text)

    places = {'places': [
        {
            "title": titles[0],
            "category": category[0],
            "addresses": addresses[0],
            "image": images[0]
        },
        {
            "title": titles[1],
            "category": category[1],
            "addresses": addresses[1],
            "image": images[1]
        },
        {
            "title": titles[2],
            "category": category[2],
            "addresses": addresses[2],
            "image": images[2]
        },
        {
            "title": titles[3],
            "category": category[3],
            "addresses": addresses[3],
            "image": images[3]
        },
        {
            "title": titles[4],
            "category": category[4],
            "addresses": addresses[4],
            "image": images[4]
        }
    ]
    }

    return places