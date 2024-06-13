from tkinter import filedialog
import ttkbootstrap as ttk
import tkinter as tk
import threading
import itertools
from pythonosc.udp_client import SimpleUDPClient
import time
import spacy
# import seaborn as sns
# nltk.download('punkt')
# nltk.download('averaged_perceptron_tagger')
# nltk.download('maxent_ne_chunker')
# nltk.download('words')
# nltk.download('vader_lexicon')
from nltk.sentiment import SentimentIntensityAnalyzer


# loading text corpora
nlp_en = spacy.load('en_core_web_lg')
nlp_de = spacy.load('de_core_news_lg')


# GUI module


class MainWindow(ttk.Window):
    # class for the app window

    def __init__(self, title, height, width):
        super().__init__()
        self.title(f'{title}')  # name
        self.geometry(f'{width}x{height}')  # size

        # setting the visual style of whole app
        # colors in hexadecimal color code
        self.configure(bg='#000000')  # background of app window
        self.style.configure('TFrame', background='#262b2b')  # color of frames
        self.style.configure('SideFrame.TFrame', background="#000000")  # color of side frames

        # styling buttons
        self.style.configure('TButton',
                             foreground='#FFFFFF',
                             background='#6C5BD1',
                             bordercolor='FFFFFF',
                             relief='RAISED',
                             font=('Arimo', 8),)

        # styling labels
        self.style.configure('TLabel',
                             foreground='#FFFFFF',
                             background='#000000',
                             font=('Arimo', 12))
        self.style.configure('Label1.TLabel',
                             foreground='#FFFFFF',
                             background='#422323',
                             font=('Arimo', 12))

    # to be invoked separately and use other objects for frames etc.
    def loopend(self):
        self.mainloop()


class ViewFrame(ttk.Frame, MainWindow):
    # class for view section containing windows

    def __init__(self, parent, relx, rely, relh, relw):
        super().__init__(parent)

        # size of frame in relative proportions
        self.relx = relx  # x coordinate
        self.rely = rely  # y coordinate
        self.relh = relh  # height
        self.relw = relw  # width

        self.lefttext = ''  # variable for left text
        self.righttext = ''  # variable for second text

        self.var1 = ttk.StringVar(value=self.lefttext)  # variable for first text, to be read by tk.Text
        self.var2 = ttk.StringVar(value=self.righttext)  # variable for second text, to be read by tk.Text
        self.var4 = ttk.StringVar(value='left value')  # variable for left value
        self.var5 = ttk.StringVar(value='right value')  # variable for right value

        self.filetext1 = ttk.StringVar(value='empty')  # variable for left text
        self.filetext2 = ttk.StringVar(value='empty')  # variable for right text

        self.config(style='SideFrame.TFrame')  # applying visual style

        self.place(relx=self.relx,
                   rely=self.rely,
                   relheight=self.relh,
                   relwidth=self.relw)

    def view_labels(self):
        # labels for view section

        label_maine = ttk.Label(self,
                                text='VIEW',
                                anchor='center',
                                background='#303030').place(relx=0,
                                                            rely=0,
                                                            relwidth=1,
                                                            relheight=0.1)

        label_values = ttk.Label(self,
                                 text='VALUES',
                                 anchor='center',
                                 background='#000000',
                                 font='Arami 10').place(relx=0.05,
                                                        rely=0.1,
                                                        relwidth=0.2,
                                                        relheight=0.1)

        label_analyze = ttk.Label(self,
                                  text='TEXT ANALYZED',
                                  anchor='center',
                                  background='#000000',
                                  font='Arami 10').place(relx=0.3,
                                                         rely=0.1,
                                                         relwidth=0.65,
                                                         relheight=0.1)

        label_left = ttk.Label(self,
                               text='l\ne\nf\nt',  # vertical text
                               font='Arami 10',
                               anchor='center',
                               background='#000000').place(relx=0,
                                                           rely=0.2,
                                                           relwidth=0.05,
                                                           relheight=0.3)

        label_right = ttk.Label(self,
                                text='r\ni\ng\nh\nt',  # vertical text
                                font='Arami 10',
                                anchor='center',
                                background='#000000').place(relx=0,
                                                            rely=0.6,
                                                            relwidth=0.05,
                                                            relheight=0.3)

        self.label_maine = label_maine
        self.label_values = label_values
        self.label_analyze = label_analyze
        self.label_left = label_left
        self.label_right = label_right

    def text_windows(self):
        # windows of view section

        window1 = tk.Text(master=self, font='Arami 10')  # window for left text
        window2 = tk.Text(master=self, font='Arami 10')  # window for right text

        window4 = tk.Text(master=self, font='Arami 30', spacing1=25)  # window for left values
        window5 = tk.Text(master=self, font='Arami 30', spacing1=25)  # window for right values

        # labels for the name of sonificated value
        label_wind1 = ttk.Label(master=self,
                                textvariable=self.var4,
                                font='Arami 10',
                                anchor='center',
                                background='#CCCCCC',
                                foreground='#000000')
        label_wind2 = ttk.Label(master=self,
                                textvariable=self.var5,
                                font='Arami 10',
                                anchor='center',
                                background='#CCCCCC',
                                foreground='#000000')

        window1.place(relx=0.3, rely=0.2, relwidth=0.65, relheight=0.3)
        window2.place(relx=0.3, rely=0.6, relwidth=0.65, relheight=0.3)

        window4.place(relx=0.05, rely=0.2, relwidth=0.2, relheight=0.3)
        window5.place(relx=0.05, rely=0.6, relwidth=0.2, relheight=0.3)

        label_wind1.place(relx=0.05, rely=0.2, relwidth=0.2, relheight=0.07)
        label_wind2.place(relx=0.05, rely=0.6, relwidth=0.2, relheight=0.07)

        # applying style
        window1.configure(fg='#000000')
        window2.configure(fg='#000000')
        window4.configure(fg='red')
        window5.configure(fg='red')

        # showing text
        window1.insert(tk.END, self.var1.get())  # fills it with a 'default' text
        window2.insert(tk.END, self.var2.get())  # fills it with a 'default' text

        self.window1 = window1
        self.window2 = window2
        self.window4 = window4
        self.window5 = window5
        self.label_wind1 = label_wind1
        self.label_wind2 = label_wind2


class DataSideFrame(ttk.Frame, MainWindow):
    # class meant to carry data processing GUI and its functionality

    def __init__(self, parent, relx, rely, relh, relw, view_object):
        super().__init__(parent)

        # size of frame in relative proportions
        self.relh = relh  # height
        self.relw = relw  # width
        self.relx = relx  # x coordinate
        self.rely = rely  # y coordinate

        # variables for file paths to be opened by Text class
        self.file_path_var1 = ttk.StringVar()
        self.file_path_var2 = ttk.StringVar()

        # variables for texts
        self.filetext1 = ttk.StringVar(value='empty')
        self.filetext2 = ttk.StringVar(value='empty')

        # variables for Text class and Sentiment class objects
        # contain default texts
        self.left_text_object_art = Text("Die promena.txt")
        self.right_text_object_art = Text("Die Verwandlung.txt")

        self.left_text_object = Sentiment("Die promena.txt")
        self.right_text_object = Sentiment("Die Verwandlung.txt")

        # object to send messages for text showing
        self.view_object = view_object

        # applying style
        self.config(style='SideFrame.TFrame')

        self.place(relx=relx, rely=self.rely, relheight=relh, relwidth=relw)

    def file_path(self, text_num):
        # method for opening the file
        # text_num 1 is left text, 2 is right text

        if text_num == 1:
            # opens file dialog
            file_path1 = filedialog.askopenfilename(title="Select A File",
                                                    filetypes=(("txt", "*.txt"),
                                                               ("mp4", "*.mp4"),
                                                               ("wmv", "*.wmv"),
                                                               ("avi", "*.avi"),
                                                               ("txt", "*.txt")))

            # setting the filepath into variable
            self.file_path_var1.set(file_path1)

        elif text_num == 2:
            # opens file dialog
            file_path2 = filedialog.askopenfilename(title="Select A File",
                                                    filetypes=(("txt", "*.txt"),
                                                               ("mp4", "*.mp4"),
                                                               ("wmv", "*.wmv"),
                                                               ("avi", "*.avi"),
                                                               ("txt", "*.txt")))

            # setting the filepath into variable
            self.file_path_var2.set(file_path2)

    def open_file(self, text_num):
        # method for processing the text file
        # text_num 1 is left text, 2 is right text

        if text_num == 1:
            text_string1 = self.file_path_var1.get()

            # creating text object
            self.left_text_object_art = Text(text_string1)
            self.left_text_object = Sentiment(text_string1)

            # setting as attribute
            filetext1 = self.left_text_object.text
            self.filetext1.set(filetext1)

        elif text_num == 2:
            text_string2 = self.file_path_var2.get()

            # creating text object
            self.right_text_object_art = Text(text_string2)
            self.right_text_object = Sentiment(text_string2)

            # setting as attribute
            filetext2 = self.right_text_object.text
            self.filetext2.set(filetext2)

    def show_file(self, text_num, view_frame):
        # method shows loaded files in windows
        # text_num 1 is left text, 2 is right text

        if text_num == 1:
            file1 = self.filetext1.get()

            # uploading text in windows
            view_frame.window1.delete('1.0', tk.END)
            view_frame.window1.insert(tk.END, file1)

        elif text_num == 2:
            file2 = self.filetext2.get()

            # uploading text in windows
            view_frame.window2.delete('1.0', tk.END)
            view_frame.window2.insert(tk.END, file2)

    def delete_file(self, text_num, view_frame):
        # method that deletes file
        # text_num 1 is left text, 2 is right text

        if text_num == 1:
            self.file_path_var1.set(value='')  # deletes the file path
            view_frame.var1.set('File deleted!!')  # stores new variable
            self.filetext1.set('empty')

            # uploading text in windows
            view_frame.window1.delete('1.0', tk.END)  # MUST BE, otherwise the line above would only be appended
            view_frame.window1.insert(tk.END, view_frame.var1.get())  # announces new state...

        elif text_num == 2:
            self.file_path_var2.set(value='')  # deletes the file path
            view_frame.var2.set('File deleted!!')  # stores new variable
            self.filetext2.set('empty')

            # uploading text in windows
            view_frame.window2.delete('1.0', tk.END)  # MUST BE, otherwise the line above would only be appended
            view_frame.window2.insert(tk.END, view_frame.var2.get())  # announce new state...

    def data_labels(self):
        # creates labels for whole data frame

        label_data = ttk.Label(master=self,
                               text='DATA',
                               anchor='center',
                               background='#303030').place(relx=0,
                                                           rely=0,
                                                           relwidth=1,
                                                           relheight=0.1)

        label_left_data = ttk.Label(master=self,
                                    text='LEFT',
                                    anchor='center',
                                    background='#000000').place(relx=0.2,
                                                                rely=0.1,
                                                                relwidth=0.2,
                                                                relheight=0.1)

        label_right_data = ttk.Label(master=self,
                                     text='RIGHT',
                                     anchor='center',
                                     background='#000000').place(relx=0.6,
                                                                 rely=0.1,
                                                                 relwidth=0.2,
                                                                 relheight=0.1)

        self.label_data = label_data
        self.label_left_data = label_left_data
        self.label_right_data = label_right_data

    def data_buttons(self):
        # creates all the buttons for data
        # two rows of four buttons
        # lambda functions evaluate methods defined above with corresponding text_num

        # LOAD Buttons
        but_load1 = ttk.Button(self, text='Load file', command=lambda: self.file_path(1))
        but_load1.place(relx=0.15, rely=0.2, relheight=0.15, relwidth=0.3)

        but_load2 = ttk.Button(self, text='Load file', command=lambda: self.file_path(2))
        but_load2.place(relx=0.55, rely=0.2, relheight=0.15, relwidth=0.3)

        # PROCESS Buttons
        but_text_str1 = ttk.Button(self, text='Process file', command=lambda: self.open_file(1))
        but_text_str1.place(relx=0.15, rely=0.4, relheight=0.15, relwidth=0.3)

        but_text_str2 = ttk.Button(self, text='Process file', command=lambda: self.open_file(2))
        but_text_str2.place(relx=0.55, rely=0.4, relheight=0.15, relwidth=0.3)

        # SHOW Buttons
        but_show1 = ttk.Button(self, text='Show file', command=lambda: self.show_file(1, self.view_object))
        but_show1.place(relx=0.15, rely=0.6, relheight=0.15, relwidth=0.3)

        but_show2 = ttk.Button(self, text='Show file', command=lambda: self.show_file(2, self.view_object))
        but_show2.place(relx=0.55, rely=0.6, relheight=0.15, relwidth=0.3)

        # DELETE Buttons
        but_delete1 = ttk.Button(self, text='Delete file', command=lambda: self.delete_file(1, self.view_object))
        but_delete1.place(relx=0.15, rely=0.8, relheight=0.15, relwidth=0.3)

        but_delete2 = ttk.Button(self, text='Delete file', command=lambda: self.delete_file(2, self.view_object))
        but_delete2.place(relx=0.55, rely=0.8, relheight=0.15, relwidth=0.3)


class SoniSideFrame(ttk.Frame, MainWindow):
    # class for sonification controllers

    def __init__(self, parent, relx, rely, relh, relw,
                 text_object_one, event_object, view_object, speed_object=None):
        super().__init__(parent)

        # size of frame in relative proportions
        self.relh = relh  # height
        self.relw = relw  # width
        self.relx = relx  # x coordinate
        self.rely = rely  # y coordinate

        # object context
        self.receiver = text_object_one  # object where text objects are stored (DataSideFrame)
        self.event_object = event_object  # object that is checked for event True/False, controls Threading
        self.view_object = view_object  # object that receives values messages (ViewFrame)
        self.speed_object = speed_object  # object in which speed is stored -> standard letter mode does not have its own

        # getting text objects of Text class
        self.text_left = self.receiver.left_text_object_art
        self.text_right = self.receiver.right_text_object_art

        # getting text objects of Sentiment class
        self.sentiment_left = self.receiver.left_text_object
        self.sentiment_right = self.receiver.right_text_object

        # default values
        self.sentiment_mode = 0
        self.polarity = 0
        self.speed = ttk.StringVar(value='0.2')
        self.event = True

        # applying style
        self.config(style='SideFrame.TFrame')

        self.place(relx=relx, rely=self.rely, relheight=relh, relwidth=relw)

    def labels_sentiment_setter(self):
        # method creates labels for sentiment setting
        # mode, polarity, speed

        label_sentiment = ttk.Label(master=self,
                                    text='SENTIMENT',
                                    font='Arami 10',
                                    anchor='center',
                                    background='#303030').place(relx=0,
                                                                rely=0,
                                                                relwidth=1,
                                                                relheight=0.1)

        label_mode = ttk.Label(master=self,
                               text='SENTIMENT',
                               font='Arami 10',
                               anchor='center',
                               background='#000000').place(relx=0.05,
                                                           rely=0.1,
                                                           relwidth=0.2,
                                                           relheight=0.1)

        label_polarity = ttk.Label(master=self,
                                   text='POLARITY',
                                   anchor='center',
                                   font='Arami 10',
                                   background='#000000').place(relx=0.05,
                                                               rely=0.5,
                                                               relwidth=0.2,
                                                               relheight=0.1)

        label_speed = ttk.Label(master=self,
                                text='SPEED',
                                anchor='center',
                                font='Arami 10',
                                background='#000000').place(relx=0.05,
                                                            rely=0.76,
                                                            relwidth=0.2,
                                                            relheight=0.1)

        self.label_sentiment = label_sentiment
        self.label_mode = label_mode
        self.label_polarity = label_polarity
        self.speed = label_speed

    def buttons_sentiment_setter(self):
        # method creates widgets for sentiment setting
        # four buttons for mode, two for polarity, text input for speed

        button_sent_pos = ttk.Button(self,
                                     text='positive',
                                     command=lambda: set_sentiment(0, 'positive', self.view_object)).place(relx=0.05,
                                                                                            rely=0.22,
                                                                                            relheight=0.05,
                                                                                            relwidth=0.2)
        button_sent_neg = ttk.Button(self,
                                     text='negative',
                                     command=lambda: set_sentiment(1, 'negative', self.view_object)).place(relx=0.05,
                                                                                            rely=0.29,
                                                                                            relheight=0.05,
                                                                                            relwidth=0.2)
        button_sent_neu = ttk.Button(self,
                                     text='neutral',
                                     command=lambda: set_sentiment(2, 'neutral', self.view_object)).place(relx=0.05,
                                                                                           rely=0.36,
                                                                                           relheight=0.05,
                                                                                           relwidth=0.2)
        button_sent_comb = ttk.Button(self,
                                      text='combined',
                                      command=lambda: set_sentiment(3, 'combined', self.view_object)).place(relx=0.05,
                                                                                             rely=0.43,
                                                                                             relheight=0.05,
                                                                                             relwidth=0.2)

        # normal polarity button -> n stands for "normal"
        button_polarity_n = ttk.Button(self,
                                       text='normal',
                                       command=lambda: set_polarity(0)).place(relx=0.05,
                                                                              rely=0.62,
                                                                              relheight=0.05,
                                                                              relwidth=0.2)

        # reversed polarity button -> r stands for "reversed"
        button_polarity_r = ttk.Button(self,
                                       text='reverse',
                                       command=lambda: set_polarity(1)).place(relx=0.05,
                                                                              rely=0.69,
                                                                              relheight=0.05,
                                                                              relwidth=0.2)

        self.speed = speed_entry = ttk.Entry(self)  # user entry field
        speed_entry.place(relx=0.05, rely=0.88, relheight=0.1, relwidth=0.2)

        self.button_sent_pos = button_sent_pos
        self.button_sent_neg = button_sent_neg
        self.button_sent_neu = button_sent_neu
        self.button_sent_comb = button_sent_comb
        self.button_polarity_n = button_polarity_n
        self.button_polarity_r = button_polarity_r

        def set_sentiment(mode, mode_name, view_object):
            # applies chosen sentiment mode
            self.sentiment_mode = mode

            # sends to windows to show what values are sonificated
            view_object.var4.set(mode_name)
            view_object.var5.set(mode_name)

        def set_polarity(mode):
            # applies chosen polarity
            self.polarity = mode

    def sentiment_modes_partial_panel(self, rely, label, function, function_two, i, mode):
        # widgets for one mode panel
        # i parameter is used for stacking panels up vertically
        # label for the mode name, button for play, button for stop

        self.label = ttk.Label(self,
                               text=str(label),
                               anchor='w',
                               font='Arami 8',
                               background='#13132C').place(relx=0.3,
                                                           rely=rely + (i * 0.15),
                                                           relh=0.05,
                                                           relw=0.65)

        self.play_button = ttk.Button(self,
                                      text='play',
                                      command=lambda: function(i,
                                                               mode,
                                                               function_two)).place(relx=0.3,
                                                                                    rely=(rely + 0.07) + (i * 0.15),
                                                                                    relh=0.05,
                                                                                    relw=0.3)

        self.stop_button = ttk.Button(self,
                                      text='stop',
                                      command=lambda: self.event_setter()).place(relx=0.65,
                                                                                 rely=(rely + 0.07) + (i * 0.15),
                                                                                 relh=0.05,
                                                                                 relw=0.3)

    def standard_modes_partial_panel(self, rely, label, function, function_two, i, mode):
        # widgets for one mode panel
        # i parameter is used for stacking widgets up vertically
        # label for the mode name, button for play, button for stop

        self.label = ttk.Label(self,
                               text=f'{label}',
                               anchor='w',
                               font='Arami 8',
                               background='#13132C').place(relx=0,
                                                           rely=rely + (i * 0.2),
                                                           relh=0.08,
                                                           relw=1)

        self.play_button = ttk.Button(self,
                                      text='play',
                                      command=lambda: function(i,
                                                               mode,
                                                               function_two)).place(relx=0,
                                                                                    rely=(rely + 0.1) + (i * 0.2),
                                                                                    relh=0.08,
                                                                                    relw=0.45)

        self.stop_button = ttk.Button(self,
                                      text='stop',
                                      command=lambda: self.event_setter()).place(relx=0.55,
                                                                                 rely=(rely + 0.1) + (i * 0.2),
                                                                                 relh=0.08,
                                                                                 relw=0.45)

    def sentiment_panel(self, rely):
        # constructs full sentiment panel
        # by iterating over dictionary and creating _partial_panel objects

        mode_main_label = ttk.Label(self,
                                    text='MODES',
                                    font='Arami 10',
                                    anchor='center',
                                    background='#000000').place(relx=0.3,
                                                                rely=0.1,
                                                                relh=0.1,
                                                                relw=0.65)
        self.mode_main_label = mode_main_label

        # dictionary for modes, can (should) be parameterized in the future
        modes_dictionary = {'button_play1': ['1: Chorus detuned', self.play],
                            'button_play2': ['2: Chorus detuned and amplitude', self.play],
                            'button_play3': ['3: Minor/major continuum', self.play],
                            'button_play4': ['4: Cutoff frequency of LPF', self.play],
                            'button_play5': ['5: Minor/major continuum and cutoff LPF frequency', self.play]}

        # iterates dictionary and fills items into sentiment_modes_partial_panel
        for index, (key, value) in enumerate(modes_dictionary.items()):
            modes_dictionary[key] = self.sentiment_modes_partial_panel(rely,
                                                                       value[0],  #  assigns first value in "modes_dictionary"
                                                                       value[1],  #  assigns second value in "modes_dictionary"
                                                                       None,  # for function two
                                                                       index,  # i parameter
                                                                       "sent")  # mode

    def soni_setter(self, func, event_object):
        # deals with threading - sets thread for 'soni'-fication

        # sets the value to True for modes to be ran again
        event_object.event = True

        # has to create new thread, because tkinter has mainloop() as process
        t = threading.Thread(target=func, args=[event_object])

        # starts the Thread
        t.start()

    def event_setter(self):  # self-explanatory
        self.event = False

    def play(self, i, mode, function_two):
        # function that plays the modes and is evaluated by play buttons

        soni_mode = i

        # getting objects
        self.sentiment_left = self.receiver.left_text_object
        self.sentiment_right = self.receiver.right_text_object

        self.text_left = self.receiver.left_text_object
        self.text_right = self.receiver.right_text_object

        # 'sent' is sentiment sonification mode
        if mode == "sent":

            # creating SonifiPy objects...
            sonifier = SonifiPy(self.view_object,
                                self.sentiment_left,
                                self.sentiment_right,
                                soni_mode=soni_mode,
                                senti_mode=self.sentiment_mode,
                                polarity=self.polarity,
                                speed=float(self.speed.get()),
                                event=self.event)

            # and calling sonification method on them
            self.soni_setter(sonifier.sentiment_analysis, eval(self.event_object))

        elif mode == "stand":
            # standard letter sonification

            # WARNING: should be recoded, only here temporarily to have one speed input field!!
            self.speed.set(self.speed_object.speed.get())

            # creating SonifiPy objects...
            sonifier = SonifiPy(self.view_object,
                                self.text_left,
                                self.text_right,
                                speed=float(self.speed.get()),
                                event=self.event)

            # and calling sonification method on them based on i as mode number
            if i == 0:  # audification
                self.soni_setter(sonifier.audi, eval(self.event_object))
            elif i == 1:  # audification with earcons
                self.soni_setter(sonifier.mixed, eval(self.event_object))
            elif i == 2:  # earcons as model
                self.soni_setter(sonifier.earcons_model, eval(self.event_object))
            elif i == 3:  # earcons as theoretical object
                self.soni_setter(sonifier.earcons, eval(self.event_object))

    def soni_modes(self, rely):
        # standard letter sonification widgets

        # label for section name
        mode_main_label = ttk.Label(master=self,
                                    text='SONIFICATION',
                                    font='Arami 10',
                                    anchor='center',
                                    background='#303030').place(relx=0,
                                                                rely=0,
                                                                relwidth=1,
                                                                relheight=0.1)


        self.mode_main_label = mode_main_label

        # dictionary for modes, should be parameterized in the future
        di_nary = {'mode_one': ['Mode 1: Audification', self.play, 'audi'],
                   'mode_two': ['Mode 2: Mixed', self.play, 'mixed'],
                   'mode_three': ['Mode 3: Earcons as Model', self.play, 'earcons_model'],
                   'mode_four': ['Mode 4: Earcons as Theoretical Object', self.play, 'earcons'],
                   }

        # iterates dictionary and fills items into standard_modes_partial_panel
        for index, (key, value) in enumerate(di_nary.items()):
            di_nary[key] = self.standard_modes_partial_panel(rely,
                                                             value[0],  # first value of dictionary: label
                                                             value[1],  # second value of dictionary: function
                                                             value[2],  # third value of dictionary: function two
                                                             index,  # i as the number of mode
                                                             "stand")  # standard letter mode


# Data module


class Text():
    # class that deals with text processing
    # kind of very lightweight version -> space for development

    def __init__(self, file):
        self.file = file
        self.text = self.text()
        self.sentences = self.sentences()
        self.words = self.words()
        self.letters = self.letters()

    def text(self):
        # opening the file

        with open(self.file, "r", encoding="utf-8") as f:
            return f.read()

    def sentences(self):
        # splitting into sentences

        return self.text.split('.')

    def words(self):
        # splitting into words

        return self.text.split()

    def letters(self):
        # splitting on letters

        letters = []
        for letter in self.text:
            letters.append(letter)
        return letters


class Sentiment(Text):
    # class that applies sentiment analysis from nltk library

    def __init__(self, text):
        super().__init__(text)

        # lists for values, names are quite clear
        self.positive = self.sentiment_calculate(self.sentences, 0)
        self.negative = self.sentiment_calculate(self.sentences, 1)
        self.neutral = self.sentiment_calculate(self.sentences, 2)
        self.combined = self.sentiment_calculate(self.sentences, 3)
        self.sentiment_list = self.sentiment_whole(self.sentences)  # has all values above in one list

    def sentences_tokenizer(self, sentence, mode):
        sia = SentimentIntensityAnalyzer()  # could be rewritten as inheritance from nltk
        snt = sia.polarity_scores(sentence)  # calculates sentiment score for the sentence

        if mode == 0:  # positive sentiment value
            return snt['pos']
        elif mode == 1:  # negative sentiment value
            return snt['neg']
        elif mode == 2:  # neutral sentiment value
            return snt['neu']
        elif mode == 3:  # compound sentiment value
            return snt['compound']

    def sentiment_calculate(self, object, mode):
        # creates lists of value for text passed

        sentiment_list = []

        for i in object:
            list_list = []
            list_list.append(i)
            list_list.append(self.sentences_tokenizer(i, mode))
            sentiment_list.append(list_list)
        return sentiment_list

    def sentiment_whole(self, object):
        # creates list of all the values

        sentiment_list_whole = []

        for i in object:
            list_list = []
            list_list.append(i)
            list_list.append(self.sentences_tokenizer(i, 0))
            list_list.append(self.sentences_tokenizer(i, 1))
            list_list.append(self.sentences_tokenizer(i, 2))
            list_list.append(self.sentences_tokenizer(i, 3))
            sentiment_list_whole.append(list_list)
        return sentiment_list_whole


# Sonification module


class SonifiPy():
    # class that deals with sonification modes -> transforming text data into messages
    # to be sent through OSC to SuperCollider

    def __init__(self, view_object, left_text, right_text, ip="127.0.0.1",
                 port=57120, soni_mode=0, senti_mode=0, polarity=0,
                 speed=0.2, event=True):

        # setting text objects
        self.left_text = left_text
        self.right_text = right_text
        self.sentiment_left = left_text.sentiment_list
        self.sentiment_right = right_text.sentiment_list

        # address for OSC
        self.ip = ip
        self.port = port

        # setting sentiment values
        self.soni_mode = soni_mode
        self.senti_mode = senti_mode
        self.polarity = polarity
        self.speed = speed

        # event for threading and object that views text
        self.event = event
        self.view_object = view_object

        # setting OSC connection
        self.client = SimpleUDPClient(ip, port)

    def send(self, msg_one, msg_two, address_one="/sentiment_analysis_left", address_two="/sentiment_analysis_right"):
        # method that sends messages on given addresses

        self.client.send_message(address_one, msg_one)
        self.client.send_message(address_two, msg_two)

    def gui_pusher_sentiment(self, mode, left, right, target):
        # method that sends data to be viewed in view section
        # for sentiment analysis sonification

        left_value = 0
        right_value = 0
        left_text_value = left[0]
        right_text_value = right[0]

        # extracting relevant sentiment value
        if mode == 0:  # positive sentiment
            left_value = left[1]
            right_value = right[1]
        elif mode == 1:  # negative sentiment
            left_value = left[2]
            right_value = right[2]
        elif mode == 2:  # neutral sentiment
            left_value = left[3]
            right_value = right[3]
        elif mode == 3:  # combined sentiment
            left_value = left[4]
            right_value = right[4]

        # deleting current values
        target.window1.delete('1.0', tk.END)
        target.window2.delete('1.0', tk.END)
        target.window4.delete('1.0', tk.END)
        target.window5.delete('1.0', tk.END)

        # filling new values
        target.window1.insert(tk.END, left_text_value)
        target.window2.insert(tk.END, right_text_value)
        target.window4.insert(tk.END, left_value)
        target.window5.insert(tk.END, right_value)

        # showing the end of line
        target.window1.see(tk.END)
        target.window2.see(tk.END)
        target.window4.see(tk.END)
        target.window5.see(tk.END)

    def gui_pusher_standard(self, left, right, target):
        # method that sends data to be viewed in view section
        # for standard letter sonification

        left_value = left
        right_value = right

        # deletes current values
        target.window4.delete('1.0', tk.END)
        target.window5.delete('1.0', tk.END)

        # inserts new values
        target.window1.insert(tk.END, left_value)
        target.window2.insert(tk.END, right_value)

        target.window4.insert(tk.END, left_value)
        target.window5.insert(tk.END, right_value)

        # showing the end of line
        target.window1.see(tk.END)
        target.window2.see(tk.END)
        target.window4.see(tk.END)
        target.window5.see(tk.END)

    def message_binder(self, sentiment, side):
        # method binds values into one msg to be sent
        # by send() method
        # for sentiment analysis sonification

        # modes, speed, polarity
        mode_soni = self.soni_mode
        mode_sent = self.senti_mode
        side = side
        speed = self.speed
        polarity = self.polarity

        # sentiment values
        pos_value = sentiment[1]
        neg_value = sentiment[2]
        neu_value = sentiment[3]
        comb_value = sentiment[4]

        # final message
        msg = [mode_soni, mode_sent, side, speed, pos_value, neg_value, neu_value, comb_value, polarity]
        return msg

    def sentiment_analysis(self, event_object):
        # sentiment analysis mode
        # iterates text and sends values in intervals defined by speed

        # event controls threading
        event_state = event_object.event

        while event_state:  # if event is True
            for left, right in zip(self.sentiment_left, self.sentiment_right):
                # creates messages
                msg_one = self.message_binder(left, 0)
                msg_two = self.message_binder(right, 1)

                # shows texts in windows
                self.gui_pusher_sentiment(self.senti_mode, left, right, self.view_object)
                self.send(msg_one, msg_two)  # sends messages

                time.sleep(self.speed)  # time interval

                event_state = event_object.event  # checks the event
                if not event_state:  # if event is false -> stops the function
                    break
                elif left[0] == '*' or right[0] == '*':
                    event_state = False
                    break

    def earcons(self, event_object):
        # 'theoretical object sonification'
        # turns letters into earcons

        # checks the event
        event_state = event_object.event
        target = self.view_object

        # deletes current values in view section
        target.window1.delete('1.0', tk.END)
        target.window2.delete('1.0', tk.END)

        # letter of sonified letters
        # 1st value is duration
        # 2nd value is morse rhythm
        letters_dict = {
            "a": [3, [1, 2]],
            "á": [3, [1, 2]],
            "ä": [6, [1, 2, 1, 2]],
            "b": [5, [2, 1, 1, 1]],
            "c": [6, [2, 1, 2, 1]],
            "č": [6, [2, 1, 2, 1]],
            "d": [4, [2, 1, 1]],
            "ď": [4, [2, 1, 1]],
            "e": [1, [1]],
            "é": [1, [1]],
            "ě": [1, [1]],
            "f": [5, [1, 1, 2, 1]],
            "g": [5, [2, 2, 1]],
            "h": [4, [1, 1, 1, 1]],
            "ch": [8, [2, 2, 2, 2]],
            "i": [2, [1, 1]],
            "í": [2, [1, 1]],
            "j": [7, [1, 2, 2, 2]],
            "k": [5, [2, 1, 2]],
            "l": [5, [1, 2, 1, 1]],
            "m": [4, [2, 2]],
            "n": [3, [2, 1]],
            "ň": [3, [2, 1]],
            "o": [6, [2, 2, 2]],
            "ó": [6, [2, 2, 2]],
            "ö": [7, [2, 2, 2, 1]],
            "p": [6, [1, 2, 2, 1]],
            "q": [7, [2, 2, 1, 2]],
            "r": [4, [1, 2, 1]],
            "ř": [4, [1, 2, 1]],
            "s": [3, [1, 1, 1]],
            "š": [3, [1, 1, 1]],
            "ß": [9, [1, 1, 1, 2, 2, 1, 1]],
            "t": [2, [2]],
            "ť": [2, [2]],
            "u": [4, [1, 1, 2]],
            "ú": [4, [1, 1, 2]],
            "ů": [4, [1, 1, 2]],
            "ü": [6, [1, 1, 2, 2]],
            "v": [5, [1, 1, 1, 2]],
            "w": [5, [1, 2, 2]],
            "x": [6, [2, 1, 1, 2]],
            "y": [7, [2, 1, 2, 2]],
            "ý": [7, [2, 1, 2, 2]],
            "z": [6, [2, 2, 1, 1]],
            "ž": [6, [2, 2, 1, 1]]
        }

        while event_state:  # if event is True
            for left_letter, right_letter in itertools.zip_longest(self.left_text.letters,
                                                                   self.right_text.letters,
                                                                   fillvalue=""):
                # views the letter in view section
                self.gui_pusher_standard(left_letter, right_letter, self.view_object)

                # if at the same index letters are not the same -> misunderstandings
                if left_letter != right_letter:

                    # l stands for "left", r stands for "right"
                    # all letters are transformed to lowercase to be consistent with dictionary
                    if left_letter.lower() not in letters_dict and right_letter.lower() in letters_dict:
                        l = 0
                        r = letters_dict[right_letter.lower()][0]
                    elif right_letter.lower() not in letters_dict and left_letter.lower() in letters_dict:
                        l = letters_dict[left_letter.lower()][0]
                        r = 0
                    elif left_letter not in letters_dict and right_letter not in letters_dict:
                        # for symbols like '.' and space
                        l = 1
                        r = 1
                    else:
                        l = letters_dict[left_letter.lower()][0]
                        r = letters_dict[right_letter.lower()][0]

                    # sends to SuperCollider
                    self.send(left_letter.lower(), right_letter.lower(), "/left", "/right")

                    # adapts break to the longer value
                    break_dur = max(l, r) * self.speed
                    time.sleep(break_dur)

                # if letters at the same index are the same -> bridges
                elif left_letter == right_letter:
                    # sends data to SuperCollider
                    self.send(left_letter.lower(), left_letter.lower(), "/both", "/both")
                    delay = 0.2
                    this_letter = left_letter.lower()  # using one letter, since they're the same

                    if left_letter not in letters_dict or right_letter not in letters_dict:
                        # for symbols like '.' and space
                        same_break = 1
                    else:
                        same_break = letters_dict[this_letter][0] * delay
                    time.sleep(same_break)  # time interval

                event_state = event_object.event  # checks for the event state
                if not event_state:  # if event is false -> stops the function
                    break
                elif left_letter == '*' or right_letter == '*':
                    event_state = False
                    break

    def earcons_model(self, event_object):
        # 'model' sonification
        # turns letters into events

        # setting the event state
        event_state = event_object.event
        target = self.view_object

        # deletes current values in view section
        target.window1.delete('1.0', tk.END)
        target.window2.delete('1.0', tk.END)

        while event_state:  # if event is True
            for left_letter, right_letter in itertools.zip_longest(self.left_text.letters,
                                                                   self.right_text.letters,
                                                                   fillvalue=""):
                # sends values to view windows
                self.gui_pusher_standard(left_letter, right_letter, self.view_object)

                # sends the message to SuperCollider
                self.send(left_letter.lower(), right_letter.lower(), "/left_model", "/right_model")

                # scales the speed accordingly
                break_dur = 1 * self.speed
                time.sleep(break_dur)  # time interval

                event_state = event_object.event  # checks for event state
                if not event_state:  # if event is false -> stops the function
                    break
                elif left_letter == '*' or right_letter == '*':
                    event_state = False
                    break

    def audi(self, event_object):
        # letter audification
        # uses UNICODE of letters

        # setting the event state
        event_state = event_object.event
        target = self.view_object

        # deletes current values in view section
        target.window1.delete('1.0', tk.END)  # clears the window
        target.window2.delete('1.0', tk.END)  # clears the window

        while event_state:  # if event is True
            for left_letter, right_letter in itertools.zip_longest(self.left_text.letters,
                                                                   self.right_text.letters,
                                                                   fillvalue=""):
                # checking for spaces
                if left_letter == '' or left_letter == None:
                    left_letter = ' '
                elif right_letter == '' or right_letter == None:
                    right_letter = ' '

                # letters to UNICODE
                msg_one = ord(left_letter)
                msg_two = ord(right_letter)

                # shows the values in the windows
                self.gui_pusher_standard(left_letter, right_letter, self.view_object)

                # sends messages to SuperCollider
                self.send(msg_one, msg_two, "/audi_left", "/audi_right")

                # time interval
                time.sleep(self.speed)

                event_state = event_object.event  # checks the event state
                if not event_state:  # checks for state to stop the function
                    break
                elif left_letter == '*' or right_letter == '*':
                    event_object.event = False
                    break

            target.window1.insert(tk.END, ' ')
            target.window2.insert(tk.END, ' ')

            event_state = event_object.event
            if not event_state:  # if event is false -> stops the function
                break


    def mixed(self, event_object):
        # mode mixes audification of UNICODE with earcon

        # setting the event state
        event_state = event_object.event
        target = self.view_object

        # letters to be 'earconed' -> parameterized in the future
        chosen_letters = ['s', 'z']

        # deletes the current value
        target.window1.delete('1.0', tk.END)  # clears the window
        target.window2.delete('1.0', tk.END)  # clears the window

        while event_state:  # if event is True
            for left_letter, right_letter in itertools.zip_longest(self.left_text.letters, self.right_text.letters, fillvalue=""):
                value_l = ""
                value_r = ""
                if left_letter == '' or left_letter == None:
                    left_letter = ' '
                elif right_letter == '' or right_letter == None:
                    right_letter = ' '

                # sets the value as 1000 if criteria met
                # must be synchronized with SuperCollider
                if left_letter.lower() in chosen_letters and right_letter.lower() in chosen_letters:
                    value_l = 1000
                    value_r = 1000

                elif left_letter.lower() in chosen_letters and right_letter.lower() not in chosen_letters:
                    value_l = 1000
                    value_r = ord(right_letter)

                elif left_letter.lower() not in chosen_letters and right_letter.lower() in chosen_letters:
                    value_l = ord(left_letter)
                    value_r = 1000

                elif left_letter.lower() not in chosen_letters and right_letter.lower() not in chosen_letters:
                    value_l = ord(left_letter)
                    value_r = ord(right_letter)

                msg_one = value_l
                msg_two = value_r

                # shows the values in the windows
                self.gui_pusher_standard(left_letter, right_letter, self.view_object)

                # sends values to SuperCollider
                self.send(msg_one, msg_two, "/earcons_left", "/earcons_right")

                time.sleep(self.speed)  # time interval

                event_state = event_object.event  # checks the event state
                if not event_state:  # checks for state to stop the function
                    break
                elif left_letter == '*' or right_letter == '*':
                    event_object.event = False
                    break

            # inserts a space
            target.window1.insert(tk.END, ' ')
            target.window2.insert(tk.END, ' ')

            event_state = event_object.event  # checks the event state
            if not event_state:  # if event is false -> stops the function
                break


try:
    window_gui = MainWindow('Bridges and Misunderstandings',
                            900,
                            2000)
    view_window = ViewFrame(window_gui,
                            0.02,
                            0.02,
                            0.46,
                            0.96)
    data_window = DataSideFrame(window_gui,
                                0.02,
                                0.5,
                                0.48,
                                0.4,
                                view_window)
    sentiment_window = SoniSideFrame(window_gui,
                                     0.44,
                                     0.5,
                                     0.48,
                                     0.25,
                                     data_window,
                                     'sentiment_window',
                                     view_window)
    standard_window = SoniSideFrame(window_gui,
                                    0.71,
                                    0.5,
                                    0.48,
                                    0.25,
                                    data_window,
                                    'standard_window',
                                    view_window,
                                    sentiment_window)

    view_window.view_labels()
    view_window.text_windows()

    data_window.data_labels()
    data_window.data_buttons()

    sentiment_window.labels_sentiment_setter()
    sentiment_window.buttons_sentiment_setter()
    sentiment_window.sentiment_panel(0.22)
                   
    standard_window.soni_modes(0.17)

except Exception as exception:
    print(exception)
finally:
    window_gui.loopend()