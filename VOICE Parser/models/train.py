with open('voice-classify.train', 'a') as f:
    while True:
        line = input('txt: ')
        print('challenge, innovation, features, opportunity, application')
        choice = input()[0]
        cat = ''
        if choice == 'c':
           cat = 'challenge '
        elif choice == 'i':
            cat = 'innovation '
        elif choice == 'f':
            cat = 'features '
        elif choice == 'o':
            cat = 'opportunity '
        elif choice == 'a':
            cat = 'application '
        elif choice == 'x':
            break
        else:
            print('nice illegal!')
            continue
        f.write(cat + line + '\n')
        print('\n')
