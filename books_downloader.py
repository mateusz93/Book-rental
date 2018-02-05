import requests
import json

SAFARI_BOOK_API_URL = 'https://www.safaribooksonline.com/api/v2/search/'
BOOKS_FILE='books.csv'

if __name__ == '__main__':
    fh = open(BOOKS_FILE, 'w', encoding="utf-8")
    page = 0
    while 1==1:
        print('Page: ' + str(page))
        response = requests.post(SAFARI_BOOK_API_URL, headers={'Content-Type': 'application/json'}, data=json.dumps({'page' : page}))
        response_as_json = json.loads(response.content)
        if response_as_json['results']:
            for book in response_as_json['results']:
                try:
                    title = book['title']
                    authors = book['authors']
                    year = str(book['issued'])[0:4]
                    author = ''
                    if len(authors) > 1:
                        author = ', '.join(str(p) for p in authors)
                    else:
                        author = authors[0]
                    fh.write(title + ';' + author + ';' + year + '\n')
                except Exception as ex:
                    print('Error')
        else:
            break
        page = page + 1
    fh.close()