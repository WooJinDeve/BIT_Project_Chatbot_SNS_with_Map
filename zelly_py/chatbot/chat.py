import pandas as pd
from sentence_transformers import SentenceTransformer
from sklearn.metrics.pairwise import cosine_similarity

model = SentenceTransformer('jhgan/ko-sroberta-multitask')

def chatbot(text):
    print(text)
    df = pd.read_csv('./chatbot/zelly_dataset.csv')
    df['embedding'] = df['유저'].map(lambda x: list(model.encode(x)))

    embedding = model.encode(text)

    df['distance'] = df['embedding'].map(lambda x: cosine_similarity([embedding], [x]).squeeze())

    answer = df.loc[df['distance'].idxmax()]

    return {"value" : answer['챗봇'], "state": answer["구분"]}

def chatbot_update():
    df = pd.read_csv('./chatbot/zelly_dataset_original.csv')
    df.head()

    df = df[~df['챗봇'].isna()]
    df.head()

    df['embedding'] = df['유저'].map(lambda x: list(model.encode(x)))
    df.head()

    df.to_csv('./chatbot/zelly_dataset.csv', index=False)