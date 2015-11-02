import xml.etree.ElementTree as ET
from bs4 import BeautifulSoup

file = ET.parse("ITTNFile.xml")
root = file.getroot()

f = ''
a = 0

def text(line):
	return line.get_text().encode('ascii', 'ignore').decode('ascii', 'ignore').lower().replace('\n', ' ')

for row in root:
	a += 1
	#print("processing number " + str(a))
	for column in row:
		if column.tag == 'WebPage':
			if column.text:
				#print(column.text)
				#t = str(column.text.encode('unicode_escape').decode('unicode_escape'))
				f += (column.text + '\n')
				thing = BeautifulSoup(column.text, 'html.parser')
				next = ''
				for line in thing.find_all(True):
					if 'class' in line.attrs:
						if line.attrs['class'][0] == 'title3':
							title = text(line)
							if 'innovation' in title:
								print('TITLE: ' + title)
								next = 'innovation'
							continue
					if next != '':
						t = text(line)
						print(text(line))
						next = ''
			break
			
print('cooking soup')
'''soup = BeautifulSoup(f, 'html.parser')
strong = soup.find_all('strong')
a = 0
next = ''
with open('voice-classify.train', 'a') as f:
	for line in soup.find_all(True):
		if 'class' in line.attrs:
			if line.attrs['class'][0] == 'title3':
				print('found title')
			elif line.attrs['class'][0] == 'title2':
				print('found body')
			
		continue
		a += 1
		if a%10 == 0:
			print("Line " + str(a))
		t = line.get_text().encode('ascii', 'ignore').decode('ascii', 'ignore').lower().replace('\n', ' ')
		if next != '':
			f.write(next + t + '\n')
			next = ''
			continue
		if 'our innovation' in t:
			next = 'innovation '
		elif 'market opportunity' in t:
			next = 'opportunity '
		
	
#a = str(soup.find_all(True)).encode('ascii', 'ignore').decode('ascii', 'ignore')
#print(a)'''