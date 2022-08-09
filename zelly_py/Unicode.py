def unitokor(text):
    text = text.encode('utf-8')
    text = text.decode('unicode_escape')
    return text