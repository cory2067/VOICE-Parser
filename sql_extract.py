import xml.etree.ElementTree as ET

file = ET.parse("export.xml")
root = file.getroot()

with open('voice-classify.train', 'a') as f:
	a = 0
	for row in root:
		a += 1
		print("processing number " + str(a))
		for column in row:
			#market opportunity
			if column.attrib['NAME'] == 'EL_VALUES_FIELD78':
					if column.text:
						t = str(column.text.encode('ascii','ignore').decode('ascii').replace('\n', ' '))
						f.write('opportunity ' + t + '\n')
						
			#our innovation
			if column.attrib['NAME'] == 'EL_VALUES_FIELD71':
					if column.text:
						t = str(column.text.encode('ascii','ignore').decode('ascii').replace('\n', ' '))
						f.write('innovation ' + t + '\n')
						
			#key features
			if column.attrib['NAME'] == 'EL_VALUES_FIELD77':
					if column.text:
						t = str(column.text.encode('ascii','ignore').decode('ascii').replace('\n', ' '))
						f.write('features ' + t + '\n')
						
			#challenge
			if column.attrib['NAME'] == 'EL_VALUES_FIELD46':
					if column.text:
						t = str(column.text.encode('ascii','ignore').decode('ascii').replace('\n', ' '))
						f.write('challenge ' + t + '\n')
						
			#projected app
			if column.attrib['NAME'] == 'EL_VALUES_FIELD95':
					if column.text:
						t = str(column.text.encode('ascii','ignore').decode('ascii').replace('\n', ' '))
						f.write('application ' + t + '\n')