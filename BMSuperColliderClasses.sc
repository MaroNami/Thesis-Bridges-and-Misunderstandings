// class that encapsulates OSC receiving code

BMReceive {
	var ip;
	var port;
	var connection;

	// connects through OSC address

	*connect {
		arg ip, port;
		var connection;
		connection = NetAddr(ip, port);
		^connection
	}

	// methods to create OSCdefs connections for particular modes
	// each returns object stored as 'connector' variable

	// method for creating an OSC receiver for one sentiment mode

	sentimentReceive {
		arg name, addName, sideText, synths;
		var connector;
		connector = OSCdef.new(name,
			{|msg, time, addr, port|

			// maps the right sentiment

			BMSentimentMapper.new.sonificationSentimentSwitcher(msg: msg, synths: synths);

		}, addName
		);
		^connector
	}

	// method for creating one receiver of model/theoretical object sonification

	artReceive {
		arg name, addName, side, pan, speed, type;
		var connector;
		connector = OSCdef.new(name, {
			|msg, time, addr, port|
			var x;

			// IMPORTANT to set incoming message as string,
			// otherwise cannot be used as dictionary key

			// checking posts - what the message is and if it is a string

			x = msg[1].asString.postln;
			x.isString.postln; // posts for check
			"".postln;

			// maps the messages values through switcher function

			BMStandardMapper.new.switcher(x, side, pan, speed, type);
		}, addName
		);
		^connector
	}

	// method for creating one receiver of standard letter sonification

	standardReceive {
		arg name, addName, mode, side, speed, synth, sequence;
		var connector;
		connector = OSCdef.new(name, {
			|msg, time, addr, port|
			var x;

			// IMPORTANT to set incoming message as string,
			// otherwise cannot be used as dictionary key

			// checking posts - what the message is and if it is a string

			x = msg[1];

			// maps the mode through switcher function

			case
			{mode==0} {BMSonifier.new.audification(x, side, speed, synth);}
			{mode==1} {BMSonifier.new.audiEarconMixed(x, side, speed, synth, sequence);};
		}, addName
		);
		^connector
	}

	// next three methods simplify code by creating several connections with parameters from list
	// arguments in list must respect index

	// for multiply sentiment modes

	sentimentAnalysis {
		arg list;
		var connector;
		connector = Array.fill(list.size, {
			arg i, obj;
			obj = this.sentimentReceive(list[i][0], list[i][1], list[i][2], list[i][3]);
			obj.postln;
		});
		^connector
	}

	// for multiply earcon model/theoretical object connections

	artisticBM {
		arg list;
		var connector;

		// creates the necessary proxy patterns -> see BMSynthetizer class

		BMSynthetizer.new.leftEarconProxies;
		BMSynthetizer.new.rightEarconProxies;

		connector = Array.fill(list.size, {
			arg i, obj;
			obj = this.artReceive(list[i][0], list[i][1], list[i][2], list[i][3], list[i][4], list[i][5]);
			obj.postln;
		});
		^connector
	}

	// for audification or mixed mode
	// sequence is for mixed mode to play an earcone for certain values

	standardBM  {
		arg list;
		var connector;
		connector = Array.fill(list.size, {
			arg i, obj;
			obj = this.standardReceive(
				name: list[i][0], // name
				addName: list[i][1], // address
				mode: list[i][2], // mode of sonification (audification or mixed)
				side: list[i][3], // side
				speed: list[i][4], // playing speed
				synth: list[i][5], // synth name
				sequence: list[i][6] // sequence of values for mixed mode
			);
			obj.postln; // post for check
		});
		^connector
	}

}

// class that encapsulates code that deals with mapping in sentiment sonification

BMSentimentMapper {
	var msg;

	// receives the message and mapps its values into variables

	messageChew {
		arg msg;
		var modeSoni, modeSent, side, speed, sentiment, polarity;

		// mapping incoming message into variables

		modeSoni = msg[1]; // mode of sonification (freq, chorus, etc...)
		modeSent = msg[2]; // mode of sentiment (positive, negative, neutral, combined)
		side = msg[3]; // side (pan), left or right
		speed = msg[4]; // speed of the sound, mapped into sustain
		sentiment = msg[5..8]; // values of sentiment analysis in order positive, negative, neutral, combined
		polarity = msg[9]; // polarity 0 or 1

		// posting values for control

		("ModeSoni is: " ++ modeSoni).postln;
		("ModeSent is: " ++ modeSent).postln;
		("Side is: " ++ side).postln;
		("Speed is: " ++ speed).postln;
		("Sentiment values are: " ++ sentiment).postln;
		("Polarity is: " ++ polarity).postln;
		("SoniObject is: " ++ polarity).postln;
		"".postln;

		// returning the values

		^[modeSoni, modeSent, side, speed, sentiment, polarity]
	}

	// receives the whole sentiment and number of mode, returns desired value according to a mode

	sentimentValueSwitcher {
		arg modeSent, sentiment;
		var sentValue;

		switch (modeSent,
		0, {sentValue = sentiment[0]}, // positive value
		1, {sentValue = sentiment[1]}, // negative value
		2, {sentValue = sentiment[2]}, // neutral value
		3, {sentValue = sentiment[3]}; // combined value
		);

		^sentValue
	}

	// receives the polarity direction, sentiment value and returns mapped valuea

	sentimentPolarizer {
		arg polarity, sentValue, fvl, fvh, fsl, fsh, amp, freq, filtFreq, modeSent;
		var lowSentScore;

		/*

		mapping of values

		-1/0 to 0.7/0.95 for lower and 1.3/1.05 for high
		0/1 to 0.95/0.99 for lower and 1.05/1.01 for high

		*/

		// combined sentiment has values from -1 to 1
		// but positive, neutral, negative from 0 to 1
		// therefore setting the lower value through lowSentScore variable

		if (modeSent == 3, {lowSentScore = -1}, {lowSentScore = 0});

		if (polarity == 0,{
			fvl = sentValue.linlin(lowSentScore, 1, 0.8, 0.99);
			fvh = sentValue.linlin(lowSentScore, 1, 1.2, 1.01);
			fsl = sentValue.linlin(lowSentScore, 1, 0.8, 0.99);
			fsh = sentValue.linlin(lowSentScore, 1, 1.2, 1.01);
			amp = sentValue.linlin(lowSentScore, 1, 0, 1);
			freq = sentValue.linlin(lowSentScore, 1, 1046.50, 1108.73);
			filtFreq = sentValue.linlin(lowSentScore, 1, 800, 5800);
		},

		{
			fvl = sentValue.linlin(lowSentScore, 1, 0.99, 0.8);
			fvh = sentValue.linlin(lowSentScore, 1, 1.01, 1.2);
			fsl = sentValue.linlin(lowSentScore, 1, 0.99, 0.8);
			fsh = sentValue.linlin(lowSentScore, 1, 1.01, 1.2);
			amp = sentValue.linlin(lowSentScore, 1, 1, 0);
			freq = sentValue.linlin(lowSentScore, 1, 1108.73, 1046.50);
			filtFreq = sentValue.linlin(lowSentScore, 1, 5800, 800);
		};
		);

		^[fvl, fvh, fsl, fsh, amp, freq, filtFreq]
	}

	// compound mapping method
	// receives message and synths
	// mapps values as desired and plays the mode

	sonificationSentimentSwitcher {
		arg msg, synths;
		var soniObject, mapper, modeSoni, modeSent, side, speed, sentiment, polarity;

		soniObject = BMSonifier.new(synths); // object for sentiment modes
		mapper = this.messageChew(msg); // 'unpacks' the values

		modeSoni = mapper[0]; // sonification mode
		modeSent = mapper[1]; // sentiment mode
		side = mapper[2];
		speed = mapper[3];

		// posts for check

		modeSoni.postln;
		modeSent.postln;
		side.postln;
		speed.postln;

		sentiment = this.sentimentValueSwitcher(modeSent, mapper[4]); // chooses the sentiment
		polarity = mapper[5];

		sentiment.postln;

		// plays the mode

		case
		{modeSoni == 0} {soniObject.sentimentModeOne(side, speed, modeSent, sentiment, polarity, synths);}
		{modeSoni == 1} {soniObject.sentimentModeTwo(side, speed, modeSent, sentiment, polarity, synths);}
		{modeSoni == 2} {soniObject.sentimentModeThree(side, speed, modeSent, sentiment, polarity, synths);}
		{modeSoni == 3} {soniObject.sentimentModeFour(side, speed, modeSent, sentiment, polarity, synths);}
		{modeSoni == 4} {soniObject.sentimentModeFive(side, speed, modeSent, sentiment, polarity, synths);}
	}
}

// class encapsulates code that deals with mapping in standard letter sonification

BMStandardMapper {
	var <>list;
	var <>dictionary;

	// default list of combined german and czech letters
	// should be parameterized in the future

	// format is [letter, duration, [morse rhythm]]

	defaultList {
		this.list = [
		["a", 3, [ 1, 2 ]],
		["á", 3, [ 1, 2 ]],
		["ä", 6, [ 1, 2, 1, 2 ]],
		["b", 5, [ 2, 1, 1, 1 ]],
		["c", 6, [ 2, 1, 2, 1 ]],
		["č", 6, [ 2, 1, 2, 1 ]],
		["d", 4, [ 2, 1, 1 ]],
		["ď", 4, [ 2, 1, 1 ]],
		["e", 1, [ 1 ]],
		["é", 1, [ 1 ]],
		["ě", 1, [ 1 ]],
		["f", 5, [ 1, 1, 2, 1 ]],
		["g", 5, [ 2, 2, 1 ]],
		["h", 4, [ 1, 1, 1, 1 ]],
		["ch", 8, [ 2, 2, 2, 2 ]],
		["i", 2, [ 1, 1 ]],
		["í", 2, [ 1, 1 ]],
		["j", 7, [ 1, 2, 2, 2 ]],
		["k", 5, [ 2, 1, 2 ]],
		["l", 5, [ 1, 2, 1, 1 ]],
		["m", 4, [ 2, 2 ]],
		["n", 3, [ 2, 1 ]],
		["ň", 3, [ 2, 1 ]],
		["o", 6, [ 2, 2, 2 ]],
		["ó", 6, [ 2, 2, 2 ]],
		["ö", 7, [ 2, 2, 2, 1 ]],
		["p", 6, [ 1, 2, 2, 1 ]],
		["q", 7, [ 2, 2, 1, 2 ]],
		["r", 4, [ 1, 2, 1 ]],
		["ř", 4, [ 1, 2, 1 ]],
		["s", 3, [ 1, 1, 1 ]],
		["š", 3, [ 1, 1, 1 ]],
		["ß", 9, [ 1, 1, 1, 2, 2, 1, 1 ]],
		["t", 2, [ 2 ]],
		["ť", 2, [ 2 ]],
		["u", 4, [ 1, 1, 2 ]],
		["ú", 4, [ 1, 1, 2 ]],
		["ů", 4, [ 1, 1, 2 ]],
		["ü", 6, [ 1, 1, 2, 2 ]],
		["v", 5, [ 1, 1, 1, 2 ]],
		["w", 5, [ 1, 2, 2 ]],
		["x", 6, [ 2, 1, 1, 2 ]],
		["y", 7, [ 2, 1, 2, 2 ]],
		["ý", 7, [ 2, 1, 2, 2 ]],
		["z", 6, [ 2, 2, 1, 1 ]],
		["ž", 6, [ 2, 2, 1, 1 ]],
	];
	}

	// default mapping dictionary for model and theoretical object sonification
	// should be parameterized in the future

	// format is:
	// letter -> [letter, language, phonological type, phonological subtype, morse rhythm, mapping parameters]

	defaultDictionary {
		this.dictionary = Dictionary[
			"a" -> ["a", "B", "V", "V", [ \Vocals_CZ, \Vocals_GE ], [ 1, 2 ], [ 60, 72 ] ],
			"á" -> ["á", "C", "V", "V", [ \Vocals_CZ, \Vocals_GE ], [ 1, 2 ], [ 60, 72 ] ],
			"ä" -> ["ä", "G", "V", "V", [ \Vocals_CZ, \Vocals_GE ], [ 1, 2, 1, 2 ], [ 60, 72 ] ],
			"b" -> ["b", "B", "C", "PLOSIVE", \Plosives, [ 2, 1, 1, 1 ], [ 0 ] ],
			"c" -> ["c", "B", "C", "AFFRICATE", \Affricates, [ 2, 1, 2, 1 ], [ 2100, 300 ] ],
			"č" -> ["č", "C", "C", "AFFRICATE", \Affricates, [ 2, 1, 2, 1 ], [ 2300, 300 ] ],
			"d" -> ["d", "B", "C", "PLOSIVE", \Plosives, [ 2, 1, 1 ], [ 1 ] ],
			"ď" -> ["ď", "C", "C", "PLOSIVE", \Plosives, [ 2, 1, 1 ], [ 2 ] ],
			"e" -> ["e", "B", "V", "V", [ \Vocals_CZ, \Vocals_GE ], [ 1 ], [ 60, 67 ] ],
			"é" -> ["é", "C", "V", "V", [ \Vocals_CZ, \Vocals_GE ], [ 1 ], [ 60, 67 ] ],
			"ě" -> ["ě", "C", "V", "V", [ \Vocals_CZ, \Vocals_GE ], [ 1 ], [ 60, 67 ] ],
			"f" -> ["f", "B", "C", "FRICATIVE", \Fricatives, [ 1, 1, 2, 1 ], [ 1000, 0.5 ] ],
			"g" -> ["g", "B", "C", "PLOSIVE", \Plosives, [ 2, 2, 1 ], [ 3 ] ],
			"h" -> ["h", "B", "C", "FRICATIVE", \Fricatives, [ 1, 1, 1, 1 ], [ 2000, 0.45 ] ],
			"ch" -> ["ch", "B", "C", "FRICATIVE", \Fricatives, [ 2, 2, 2, 2 ], [ 3000, 0.4 ] ],
			"i" -> ["i", "B", "V", "V", [ \Vocals_CZ, \Vocals_GE ], [ 1, 1 ], [ 60, 63 ] ],
			"í" -> ["í", "C", "V", "V", [ \Vocals_CZ, \Vocals_GE ], [ 1, 1 ], [ 60, 63 ] ],
			"j" -> ["j", "B", "C", "APPROXIMANT", \Approximantes, [ 1, 2, 2, 2 ], [ [ 62, 74 ], [ 5800, 200 ] ] ],
			"k" -> ["k", "B", "C", "PLOSIVE", \Plosives, [ 2, 1, 2 ], [ 4 ] ],
			"l" -> ["l", "B", "C", "APPROXIMANT", \Approximantes, [ 1, 2, 1, 1 ], [ [ 58, 70] , [ 5800, 200 ] ] ],
			"m" -> ["m", "B", "C", "NASAL", \Nasals, [ 2, 2 ], [ 57, 300, 600, 600, 1600 ] ],
			"n" -> ["n", "B", "C", "NASAL", \Nasals, [ 2, 1 ], [ 60, 300, 600, 800, 1800] ],
			"ň" -> ["ň", "C", "C", "NASAL", \Nasals, [ 2, 1 ], [ 64, 300, 600, 900, 1900 ] ],
			"o" -> ["o", "B", "V", "V", [ \Vocals_CZ, \Vocals_GE ], [ 2, 2, 2 ], [ 60, 65 ] ],
			"ó" -> ["ó", "C", "V", "V", [ \Vocals_CZ, \Vocals_GE ], [ 2, 2, 2 ], [ 60, 65 ] ],
			"ö" -> ["ö", "G", "V", "V", [ \Vocals_CZ, \Vocals_GE ], [ 2, 2, 2, 1 ], [ 60, 65 ] ],
			"p" -> ["p", "B", "C", "PLOSIVE", \Plosives, [ 1, 2, 2, 1 ], [ 1 ] ],
			"q" -> ["q", "B", "C", "PLOSIVE", \Plosives, [ 2, 2, 1, 2 ], [ 6 ] ],
			"r" -> ["r", "B", "C", "VIBRANT", \Vibrants, [ 1, 2, 1 ], [ 1200 ] ],
			"ř" -> ["ř", "C", "C", "VIBRANT", \Vibrants, [ 1, 2, 1 ], [ 120 ] ],
			"s" -> ["s", "B", "C", "FRICATIVE", \Fricatives, [ 1, 1, 1 ], [ 4000, 0.35 ] ],
			"š" -> ["š", "C", "C", "FRICATIVE", \Fricatives, [ 1, 1, 1 ], [ 5000, 0.3 ] ],
			"ß" -> ["ß", "G", "C", "FRICATIVE", \Fricatives, [ 1, 1, 1, 2, 2, 1, 1 ], [ 100, 0.05 ] ],
			"t" -> ["t", "B", "C", "PLOSIVE", \Plosives, [ 2 ], [ 0 ] ],
			"ť" -> ["ť", "C", "C", "PLOSIVE", \Plosives, [ 2 ], [ 8 ] ],
			"u" -> ["u", "B", "V", "V", [ \Vocals_CZ, \Vocals_GE ], [ 1, 1, 2 ], [ 60, 62 ] ],
			"ú" -> ["ú", "C", "V", "V", [ \Vocals_CZ, \Vocals_GE ], [ 1, 1, 2 ], [ 60, 62 ] ],
			"ů" -> ["ů", "C", "V", "V", [ \Vocals_CZ, \Vocals_GE ], [ 1, 1, 2 ], [ 60, 62 ] ],
			"ü" -> ["ü", "G", "V", "V", [ \Vocals_CZ, \Vocals_GE ], [ 1, 1, 2, 2 ], [ 60, 62 ] ],
			"v" -> ["v", "B", "C", "FRICATIVE", \Fricatives, [ 1, 1, 1, 2 ], [ 7000, 0.25 ] ],
			"w" -> ["w", "B", "C", "FRICATIVE", \Fricatives, [ 1, 2, 2 ], [ 8000, 0.2 ] ],
			"x" -> ["x", "B", "C", "FRICATIVE", \Fricatives, [ 2, 1, 1, 2 ], [ 9000, 0.6 ] ],
			"y" -> ["y", "B", "V", "V", [ \Vocals_CZ, \Vocals_GE ], [ 2, 1, 2, 2 ], [ 60, 64 ] ],
			"ý" -> ["ý", "C", "V", "V", [ \Vocals_CZ, \Vocals_GE ], [ 2, 1, 2, 2 ], [ 60, 64 ] ],
			"z" -> ["z", "B", "C", "FRICATIVE", \Fricatives, [ 2, 2, 1, 1 ], [ 10000, 0.15 ] ],
			"ž" -> ["ž", "C", "C", "FRICATIVE", \Fricatives, [ 2, 2, 1, 1 ], [ 11000, 0.1 ] ],
		];
	}

	// switches sides

	switcher {
		arg x, side, pan, speed, type;

		// mapping of sides is in order left-right-both

		case
		{side == 0} {BMStandardMapper.new.leftSetter(x, pan, speed, type);
			BMSynthetizer.leftEarcon;}
		{side == 1} {BMStandardMapper.new.rightSetter(x, pan, speed, type);
			BMSynthetizer.rightEarcon;}
		{side == 2} {BMStandardMapper.new.bothSetter(x, pan, speed);
			BMSynthetizer.leftEarcon;
			BMSynthetizer.rightEarcon;};
	}

	// methods for changing the parameters of proxy patterns

	leftSetter {
		arg x, pan, speed, type;
		var a, p, dict, duration, theoModelDur;

		dict = this.defaultDictionary;
		dict = dict.dictionary;
		theoModelDur = dict[x][5];
		theoModelDur.postln;

		// switching phonosemantic model and theoretical object
		case
		{type == 'model'} {duration = [1]}
		{type != 'model'} {duration = theoModelDur};

		a = dict[x][3]; // getting phonetic subcategory
		p = speed; // speeding the duration: WARNING - must be synchronized with Python

		// switches arguments based on phonetic subcategory, see defaultDictionary method
		// side is reflected in the name (_l)

		switch (a,
			"V", {Pdefn(\instrument_l, dict[x][4][0]);
				Pdefn(\pan_l, pan);
				Pdefn(\dur_l, Pseq(duration*p, 1));
				Pdefn(\freq_l, Pseq([dict[x][6]], inf).midicps);
				Pdefn(\buf_l, 0);
				Pdefn(\freqFilOne_l, 0);
				Pdefn(\freqFilTwo_l, 0);
				Pdefn(\rel_l, 1*p);},
			"PLOSIVE", {Pdefn(\instrument_l, dict[x][4]);
				Pdefn(\pan_l, pan);
				Pdefn(\dur_l, Pseq(duration*p, 1));
				Pdefn(\buf_l, dict[x][6]);
			},
			"AFFRICATE", {
				Pdefn(\instrument_l, dict[x][4]);
				Pdefn(\dur_l, Pseq(duration*p, 1));
				Pdefn(\pan_l, pan);
				Pdefn(\freqFilOne_l, dict[x][6][0]);
				Pdefn(\freqFilTwo_l, dict[x][6][1]);
				Pdefn(\sus_l, 1*p);
			},
			"FRICATIVE", {
				Pdefn(\instrument_l, dict[x][4]);
				Pdefn(\dur_l, Pseq(duration*p, 1));
				Pdefn(\pan_l, pan);
				Pdefn(\freqFilOne_l, dict[x][6][0]);
				Pdefn(\rel_l, 0.1*p);
				Pdefn(\sus_l, dict[x][6][1]*p);
			},
			"APPROXIMANT", {
				Pdefn(\instrument_l, dict[x][4]);
				Pdefn(\dur_l, Pseq(duration*p, 1));
				Pdefn(\pan_l, pan);
				Pdefn(\freq_l, Pseq([dict[x][6][0]], 1).midicps);
				Pdefn(\freqFilOne_l, dict[x][6][1][0]);
				Pdefn(\freqFilTwo_l, dict[x][6][1][1]);
				Pdefn(\rel_l, 1*p);
				Pdefn(\sus_l, 0.5*p);
			},
			"NASAL", {Pdefn(\instrument_l, dict[x][4]);
				Pdefn(\dur_l, Pseq(duration*p, 1));
				Pdefn(\pan_l, pan);
				Pdefn(\freq_l, Pseq([dict[x][6][0]], 1).midicps);
				Pdefn(\freqFilOne_l, dict[x][6][1]);
				Pdefn(\freqFilTwo_l, dict[x][6][2]);
				Pdefn(\rel_l, 1*p);
				Pdefn(\sus_l, 0.5*p);
			},
			"VIBRANT", {
				Pdefn(\instrument_l, dict[x][4]);
				Pdefn(\pan_l, pan);
				Pdefn(\dur_l, Pseq(duration*p, 1));
				Pdefn(\freq_l, Pseq([dict[x][6]], inf));
				Pdefn(\rel_l, 1*p);
				Pdefn(\sus_l, 0.5*p);
			},
		);
		^[a, dict]
	}

	rightSetter {
		arg y, pan, speed, type;
		var b, q, dict, duration, theoModelDur;

		dict = this.defaultDictionary;
		dict = dict.dictionary;
		theoModelDur = dict[y][5];
		theoModelDur.postln;

		// switching phonosemantic model and theoretical object
		case
		{type == 'model'} {duration = [1]}
		{type != 'model'} {duration = theoModelDur};

		b = dict[y][3].postln; // getting phonetic subcategory
		q = speed;

		// switches arguments based on phonetic subcategory, see defaultDictionary method
		// side is reflected in the name (_r)

		switch (b,
			"V", {Pdefn(\instrument_r, dict[y][4][1]);
				Pdefn(\pan_r, pan);
				Pdefn(\dur_r, Pseq(duration*q, 1));
				Pdefn(\freq_r, Pseq([dict[y][6]], inf).midicps);
				Pdefn(\buf_r, 0);
				Pdefn(\freqFilOne_r, 0);
				Pdefn(\freqFilTwo_r, 0);
				Pdefn(\rel_r, 1*q);},
			"PLOSIVE", {Pdefn(\instrument_r, dict[y][4]);
				Pdefn(\pan_r, pan);
				Pdefn(\dur_r, Pseq(duration*q, 1).trace);
				Pdefn(\buf_r, dict[y][6]);
			},
			"AFFRICATE", {
				Pdefn(\instrument_r, dict[y][4]);
				Pdefn(\dur_r, Pseq(duration*q, 1));
				Pdefn(\pan_r, pan);
				Pdefn(\freqFilOne_r, dict[y][6][0]);
				Pdefn(\freqFilTwo_r, dict[y][6][1]);
				Pdefn(\rel_r, 1*q);
				Pdefn(\sus_r, 1*q);
			},
			"FRICATIVE", {
				Pdefn(\instrument_r, dict[y][4]);
				Pdefn(\dur_r, Pseq(duration*q, 1));
				Pdefn(\pan_r, pan);
				Pdefn(\freqFilTwo_r, dict[y][6][0]);
				Pdefn(\rel_r, 0.1*q);
				Pdefn(\sus_r, dict[y][6][1]*q);
			},
			"APPROXIMANT", {
				Pdefn(\instrument_r, dict[y][4]);
				Pdefn(\dur_r, Pseq(duration*q, 1));
				Pdefn(\pan_r, pan);
				Pdefn(\freq_r, Pseq([dict[y][6][0]], 1).midicps);
				Pdefn(\freqFilOne_r, dict[y][6][1][0]);
				Pdefn(\freqFilTwo_r, dict[y][6][1][1]);
				Pdefn(\rel_r, 1*q);
				Pdefn(\sus_r, 0.5*q);
			},
			"NASAL", {Pdefn(\instrument_r, dict[y][4]);
				Pdefn(\dur_r, Pseq(duration*q, 1));
				Pdefn(\pan_r, pan);
				Pdefn(\freq_r, Pseq([dict[y][6][0]], 1).midicps);
				Pdefn(\freqFilOne_r, dict[y][6][3]);
				Pdefn(\freqFilTwo_r, dict[y][6][4]);
				Pdefn(\rel_r, 1*q);
				Pdefn(\sus_r, 0.5*q);
			},
			"VIBRANT", {
				Pdefn(\instrument_r, dict[y][4]);
				Pdefn(\pan_r, pan);
				Pdefn(\dur_r, Pseq(duration*q, 1));
				Pdefn(\freq_r, Pseq([dict[y][6]], inf));
				Pdefn(\rel_r, 0*q);
				Pdefn(\sus_r, 0.5*q);
			},
		);
		^[b, dict]
	}

	// stereo for the same letter -> bridges

	bothSetter {
		arg z, pan, speed;
		var c, r, dict;

		dict = this.defaultDictionary;
		dict = dict.dictionary;

		c = dict[z][3]; // getting phonetic subcategory
		r = speed;

		// switches arguments based on phonetic subcategory, see defaultDictionary method
		// side is reflected in the name (_r and _l)

		switch (c,
			"V", {Pdefn(\instrument_l, dict[z][4][0]);
				Pdefn(\pan_l, pan[0]);
				Pdefn(\dur_l, Pseq(dict[z][5]*r, 2));
				Pdefn(\freq_l, Pseq(dict[z][6] + 12, inf).midicps);
				Pdefn(\buf_l, 0);
				Pdefn(\freqFilOne_l, 0);
				Pdefn(\freqFilTwo_l, 0);
				Pdefn(\rel_l, 1);

				Pdefn(\instrument_r, dict[z][4][1]);
				Pdefn(\pan_r, pan[1]);
				Pdefn(\dur_r, Pseq(dict[z][5]*r, 2));
				Pdefn(\freq_r, Pseq(dict[z][6] + 12, inf).midicps);
				Pdefn(\buf_r, 0);
				Pdefn(\freqFilOne_r, 0);
				Pdefn(\freqFilTwo_r, 0);
				Pdefn(\rel_r, 1);
			},
			"PLOSIVE", {Pdefn(\instrument_l, dict[z][4]);
				Pdefn(\pan_l, pan[0]);
				Pdefn(\dur_l, Pseq(dict[z][5]*r, 2));
				Pdefn(\buf_l, dict[z][6]);

				Pdefn(\instrument_r, dict[z][4]);
				Pdefn(\pan_r, pan[1]);
				Pdefn(\dur_r, Pseq(dict[z][5]*r, 2).trace);
				Pdefn(\buf_r, dict[z][6]);
			},
			"AFFRICATE", {
				Pdefn(\instrument_l, dict[z][4]);
				Pdefn(\dur_l, Pseq(dict[z][5]*r, 2));
				Pdefn(\pan_l, pan[0]);
				Pdefn(\freqFilOne_l, dict[z][6][0]);
				Pdefn(\freqFilTwo_l, dict[z][6][1]);

				Pdefn(\instrument_r, dict[z][4]);
				Pdefn(\dur_r, Pseq(dict[z][5]*r, 2));
				Pdefn(\pan_r, pan[1]);
				Pdefn(\freqFilOne_r, dict[z][6][0]);
				Pdefn(\freqFilTwo_r, dict[z][6][1]);
			},
			"FRICATIVE", {
				Pdefn(\instrument_l, dict[z][4]);
				Pdefn(\dur_l, Pseq(dict[z][5]*r, 2));
				Pdefn(\pan_l, pan[0]);
				Pdefn(\freqFilOne_l, dict[z][6][0]);
				Pdefn(\rel_l, 0);
				Pdefn(\sus_l, 0.1);

				Pdefn(\instrument_r, dict[z][4]);
				Pdefn(\dur_r, Pseq(dict[z][5]*r, 2));
				Pdefn(\pan_r, pan[1]);
				Pdefn(\freqFilOne_r, dict[z][6][0]);
				Pdefn(\rel_r, 0);
				Pdefn(\sus_l, 0.1);
			},
			"APPROXIMANT", {
				Pdefn(\instrument_l, dict[z][4]);
				Pdefn(\dur_l, Pseq(dict[z][5]*r, 2));
				Pdefn(\pan_l, pan[0]);
				Pdefn(\freq_l, Pseq(dict[z][6][0], inf).midicps);
				Pdefn(\freqFilOne_l, dict[z][6][1][0]);
				Pdefn(\freqFilTwo_l, dict[z][6][1][1]);

				Pdefn(\instrument_r, dict[z][4]);
				Pdefn(\dur_r, Pseq(dict[z][5]*r, 2));
				Pdefn(\pan_r, pan[1]);
				Pdefn(\freq_r, Pseq(dict[z][6][0], inf).midicps);
				Pdefn(\freqFilOne_r, dict[z][6][1][0]);
				Pdefn(\freqFilTwo_r, dict[z][6][1][1]);
			},
			"NASAL", {Pdefn(\instrument_l, dict[z][4]);
				Pdefn(\dur_l, Pseq(dict[z][5]*r, 2));
				Pdefn(\pan_l, pan[0]);
				Pdefn(\freq_l, Pseq([dict[z][6][0]], inf).midicps);
				Pdefn(\freqFilOne_l, dict[z][6][1]);
				Pdefn(\freqFilTwo_l, dict[z][6][2]);

				Pdefn(\instrument_r, dict[z][4]);
				Pdefn(\dur_r, Pseq(dict[z][5]*r, 2));
				Pdefn(\pan_r, pan[1]);
				Pdefn(\freq_r, Pseq([dict[z][6][0]], inf).midicps);
				Pdefn(\freqFilOne_r, dict[z][6][3]);
				Pdefn(\freqFilTwo_r, dict[z][6][4]);
			},
			"VIBRANT", {
				Pdefn(\instrument_l, dict[z][4]);
				Pdefn(\pan_l, pan[0]);
				Pdefn(\dur_l, Pseq(dict[z][5]*r, 2));
				Pdefn(\freq_l, Pseq([dict[z][6]], inf));
				Pdefn(\rel_l, 0);

				Pdefn(\instrument_r, dict[z][4]);
				Pdefn(\pan_r, pan[1]);
				Pdefn(\dur_r, Pseq(dict[z][5]*r, 2));
				Pdefn(\freq_r, Pseq([dict[z][6]], inf));
				Pdefn(\rel_r, 0);
			},
		);
		^[c, dict]
	}
}

// class that encapsulates mode players

BMSonifier {
	var side=0, speed=0.5, modeSent=0, polarity=0;
	var fvl=0, fvh=0, fsl=0, fsh=0, amp=1;

	// five methods for five modes
	// each takes arguments: side for panning, speed for sound duration, modeSent as sentiment mode,
	// sentiment as sentiment values, polarity as mapping direction and synths as sounds to be played
	// each uses BMSentimentMapper.new.sentimentPolarizer for mapping incoming values into sound parameters

	sentimentModeOne {
		arg side, speed, modeSent, sentiment, polarity, synths;
		var fvl, fvh, fsl, fsh, amp, mappedValues;

		// maps values
		mappedValues = BMSentimentMapper.new.sentimentPolarizer(polarity, sentiment, fvl, fvh, fsl, fsh, amp, modeSent: modeSent);

		fvl = mappedValues[0];
		fvh = mappedValues[1];
		fsl = mappedValues[2];
		fsh = mappedValues[3];

		// speed is 'intended' a little bit to avoid clicking
		speed = speed + 0.02;


		// player
		case
		{side == 0} {
			BMSynthetizer.new.sentimentPlay(
				synths[0],
				synths[0],
				[freq: 440, atk: 0.01, sus: speed, fvl: fvl, fvh: fvh, fsl:fsl, fsh:fsh],
				[freq: 659.25, atk: 0.01, sus: speed, fvl: fvl, fvh: fvh, fsl:fsl, fsh:fsh]);
			}
		{side == 1} {
			BMSynthetizer.new.sentimentPlay(
				synths[1],
				synths[1],
				[freq: 440, atk: 0.01, sus: speed, fvl: fvl, fvh: fvh, fsl:fsl, fsh:fsh],
				[freq: 659.25, atk: 0.01, sus: speed, fvl: fvl, fvh: fvh, fsl:fsl, fsh:fsh]);
		}
	}

	sentimentModeTwo {
		arg side, speed, modeSent, sentiment, polarity, synths;
		var fvl, fvh, fsl, fsh, amp, mappedValues;

		// maps values
		mappedValues = BMSentimentMapper.new.sentimentPolarizer(polarity, sentiment, fvl, fvh, fsl, fsh, amp, modeSent: modeSent);

		fvl = mappedValues[0];
		fvh = mappedValues[1];
		fsl = mappedValues[2];
		fsh = mappedValues[3];
		amp = mappedValues[4];

		// speed is 'intended' a little bit to avoid clicking
		speed = speed + 0.02;

		// player
		case
		{side == 0} {
			BMSynthetizer.new.sentimentPlay(
				synths[0],
				synths[0],
				[freq: 440, atk: 0.01, sus: speed, fvl: fvl, fvh: fvh, fsl:fsl, fsh:fsh, amp:amp],
				[freq: 659.25, atk: 0.01, sus: speed, fvl: fvl, fvh: fvh, fsl:fsl, fsh:fsh, amp:amp]);
			}
		{side == 1} {
			BMSynthetizer.new.sentimentPlay(
				synths[1],
				synths[1],
				[freq: 440, atk: 0.01, sus: speed, fvl: fvl, fvh: fvh, fsl:fsl, fsh:fsh, amp:amp],
				[freq: 659.25, atk: 0.01, sus: speed, fvl: fvl, fvh: fvh, fsl:fsl, fsh:fsh, amp:amp]);
		}
	}

	sentimentModeThree {
		arg side, speed, modeSent, sentiment, polarity, synths;
		var fvl=0, fvh=0, fsl=0, fsh=0, amp=0, mappedValues, freq;

		// maps values
		mappedValues = BMSentimentMapper.new.sentimentPolarizer(polarity, sentiment, fvl, fvh, fsl, fsh, amp, modeSent: modeSent);

		freq = mappedValues[5];
		freq.postln;

		// speed is 'intended' a little bit to avoid clicking
		speed = speed + 0.02;

		// player
		case
		{side == 0} {
			BMSynthetizer.new.sentimentPlay(
				synths[0],
				synths[0],
				[freq: 880, atk: 0.01, sus: speed],
				[freq: freq, atk: 0.01, sus: speed]);
		}
		{side == 1} {
			BMSynthetizer.new.sentimentPlay(
				synths[1],
				synths[1],
				[freq: 880, atk: 0.01, sus: speed],
				[freq: freq, atk: 0.01, sus: speed]);
		}
	}

	sentimentModeFour {
		arg side, speed, modeSent, sentiment, polarity, synths;
		var fvl=0, fvh=0, fsl=0, fsh=0, amp=0, mappedValues, freq, filtFreq;

		// maps values
		mappedValues = BMSentimentMapper.new.sentimentPolarizer(polarity, sentiment, fvl, fvh, fsl, fsh, amp, freq, filtFreq, modeSent: modeSent);

		filtFreq = mappedValues[6];

		// speed is 'intended' a little to avoid clicking
		speed = speed + 0.02;

		// player
		case
		{side == 0} {Synth(synths[0], [freq: 880, atk: 0.01, sus: speed, filtFreq: filtFreq]);}
		{side == 1} {Synth(synths[1], [freq: 880, atk: 0.01, sus: speed, filtFreq: filtFreq]);}
	}

	sentimentModeFive {
		arg side, speed, modeSent, sentiment, polarity, synths;
		var fvl=0, fvh=0, fsl=0, fsh=0, amp=0, sentValue, mappedValues, freq, filtFreq;

		// maps values
		mappedValues = BMSentimentMapper.new.sentimentPolarizer(polarity, sentiment, fvl, fvh, fsl, fsh, amp, freq, filtFreq, modeSent: modeSent);

		freq = mappedValues[5];
		filtFreq = mappedValues[6];

		// speed is 'intended' a little bit to avoid clicking
		speed = speed + 0.02;

		// player
		case
		{side == 0} {
			BMSynthetizer.new.sentimentPlay(
				synths[0],
				synths[0],
				[freq: 880, atk: 0.01, sus: speed, filtFreq: filtFreq],
				[freq: freq, atk: 0.01, sus: speed, filtFreq: filtFreq]);
		}
		{side == 1} {
			BMSynthetizer.new.sentimentPlay(
				synths[1],
				synths[1],
				[freq: 880, atk: 0.01, sus: speed, filtFreq: filtFreq],
				[freq: freq, atk: 0.01, sus: speed, filtFreq: filtFreq]);
		}
	}

	audification {
		arg value, side, speed, synth;
		var x, lowVal, highVal, lowFreq, highFreq, pan;

		// speed is 'intended' a little to avoid clicking
		speed = speed + 0.02;

		// next value may well be parameterized in the future

		lowVal=97; // lowest incomming UNICODE
		highVal=382; // highest incoming UNICODE
		lowFreq=220; // lowest mapped frequency
		highFreq=880; // highest mapped frequency

		case
		{side==0} {pan = 1}
		{side==1} {pan = -1};

		// maps incoming UNICODE to freqency, from linear range to linear
		x = value.linlin(lowVal, highVal, lowFreq, highFreq);
		Synth(synth, [freq: x, pan:pan, amp:0.5, atk:0.01, sus:speed, rel:0.01]);

		// posts for check
		("Value " ++ value ++ " is mapped to freq " ++ x ++ ".").postln;

		^[value, x]
	}

	audiEarconMixed {
		arg value, side, speed, synth, sequence;
		var x, lowVal, highVal, lowFreq, highFreq, pan, earcone, divider;

		// see audification method above

		lowVal=97; // lowest incomming UNICODE
		highVal=382; // highest incoming UNICODE
		lowFreq=220; // lowest mapped frequency
		highFreq=880; // highest mapped frequency

		// speed is 'intended' a little to avoid clicking
		speed = speed + 0.02;

		case
		{side==0} {pan = 1}
		{side==1} {pan = -1};

		// divides the whole earcon notes duration equally according to list size
		// you can see it in (speed/divider) few lines further
		divider = sequence.size;

		// Routine to play the earcon
		earcone = Routine.new({
				divider.do({
				arg i;
				var	seq;
				seq = sequence;
				seq[i].postln;
				(speed/divider).wait;
				Synth(synth, [freq: seq[i].midicps, pan:pan, amp:0.5, atk:0.01, sus: (speed/divider), rel:0.01]);
				})
		});

		// value 1000 is convention and must be synchronized with Python!!
		// must be different than other UNICODE values
		if ( value == 1000,
			{earcone.play;
				"1000!!!!!!!!!!".postln; // posts for check
			},
			{x = value.linlin(lowVal, highVal, lowFreq, highFreq);
				Synth(synth, [freq: x, pan:pan, amp:0.5, atk:0.01, sus:speed, rel:0.01]);
				("X is " ++ x).postln;
			}
		);

		// posts for check
		("Value " ++ value ++ " is mapped to freq " ++ x ++ ".").postln;

		^[value, x, sequence]
	}
}

// class that encapsulated synthetizer sounds needed for modes

BMSynthetizer {

	// synths for sentiment analysis (all modes)

	*sentimentSynths {
		SynthDef.new(\Pad_left, {
			arg freq=400,
			sus=1, atk=0.01, rel=0.01,
			pan=0.9,
			amp=1,

			// default: fvl=0.97, fvh=1.02, fsl=0.98, fsh=1.02

			fvl=0.97, // lower detune value
			fvh=1.02, // high detune value
			fsl=0.98, // lower detune value
			fsh=1.02, // high detune value

			filtFreq=800, // LPF cutoff frequency
			rq=1; // LPF reciprocal

			var temp, temp_two, sum, env;

			env = EnvGen.ar(Env([0, 1, 1, 0], [atk, sus, rel], [1, -1]), doneAction:2);

			sum = 0;

			// creates chorus effect by randomly detuning each tone
			7.do{
				temp = LFSaw.ar(
					freq * {Rand(fvl, fvh)},
					{Rand(0.02, 2)},
					{ExpRand(0.05, 0.01)};
				);
				sum = sum + temp;
			};

			// creates chorus effect by randomly detuning each tone
			16.do{
				temp_two = LFSaw.ar(freq * {Rand(fsl, fsh)},
					{Rand(0.7, 2)},
					0.6,
					0.1;
				);
				sum = sum + temp_two;
			};

			sum = RLPF.ar(sum, filtFreq, rq);
			sum = sum * env * 0.015;
			sum = Pan2.ar(sum, pan, amp);

			Out.ar(0, sum);
		}).add;

		SynthDef.new(\Pad_right, {
			arg freq=400,
			sus=1, atk=0.01, rel=0.01,
			pan=(-0.9),
			amp=1,

			// default: fvl=0.97, fvh=1.02, fsl=0.98, fsh=1.02

			fvl=0.97, // lower detune value
			fvh=1.02, // high detune value
			fsl=0.98, // lower detune value
			fsh=1.02, // high detune value

			filtFreq = 800, // LPF cutoff frequency
			rq = 1; // LPF reciprocal

			var temp, temp_two, sum, env;

			env = EnvGen.ar(Env([0, 1, 1, 0],[atk, sus, rel],[1, -1]), doneAction:2);

			sum = 0;

			// creates chorus effect by randomly detuning each tone
			7.do{
				temp = LFTri.ar(
					freq * {Rand(fvl, fvh)},
					{Rand(0.02, 2)},
					{ExpRand(0.05, 0.01)};
				);
				sum = sum + temp;
			};

			// creates chorus effect by randomly detuning each tone
			16.do{
				temp_two = LFTri.ar(freq * {Rand(fsl, fsh)},
					{Rand(0.7, 2)},
					0.6,
					0.1;
				);
				sum = sum + temp_two;
			};

			sum = RLPF.ar(sum, filtFreq, rq);
			sum = sum * env * 0.015;
			sum = Pan2.ar(sum, pan, amp);

			Out.ar(0, sum);
		}).add;
		^[\Pad_left, \Pad_right]
	}

	// synths for audification and mixed sonification

	*standardSynths {
		SynthDef.new(\AudiLeft, {
			|freq=440, atk=0.001, sus=0.1, rel=0.001, amp=1, pan=0|
			var sig, env;

			env = EnvGen.ar(Env.new([0, 1, 1, 0],[atk, sus, rel],[1, -1]), doneAction: 2);

			sig = SinOsc.ar(freq);
			sig = sig * env * amp;
			sig = Pan2.ar(sig, pan);

			Out.ar(0, sig);
		}).add;

		SynthDef.new(\AudiRight, {
			|freq=440, atk=0.001, sus=0.1, rel=0.001, amp=1, pan=0|
			var sig, env;

			env = EnvGen.ar(Env.new([0, 1, 1, 0],[atk, sus, rel],[1, -1]), doneAction:2);

			sig = VarSaw.ar(freq);
			sig = sig * env * amp;
			sig = Pan2.ar(sig, pan);

			Out.ar(0, sig);
		}).add;
		^[\AudiLeft, \AudiRight]

	}

	// synths for model/theoretical object

	*artSynths {
		SynthDef(\Vocals_CZ, {
			arg freq=880, pan=1, amp=1, ampenv=1,
			atk=0, rel=1;

			var sig, temp, sum, env;

			sum = 0;
			env = EnvGen.kr(Env.perc(atk, rel, ampenv, 1), doneAction:2);

			// creates chorus effect by randomly detuning each tone
			20.do{
				temp = SinOsc.ar(
					freq * {Rand(0.98, 1.02)},
					{Rand(0.0, 1.0)},
					{ExpRand(0.005, 0.05)};
				);
				sum = sum + temp;
			};

			sig = sum;
			sig = RLPF.ar(sig, 800, 0.03, 0.3);
			sig = Pan2.ar(sig, pan);
			sig = sig * env * amp;

			Out.ar(0, sig);
		}).add;

		//____________________  II.

		SynthDef(\Vocals_GE, {
			arg freq=880, pan=(-1), amp=2, ampenv=1,
			atk=0, rel=0.5;

			var sig, temp, sum, env;

			sum = 0;
			env = EnvGen.kr(Env.perc(atk, rel, ampenv, 1), doneAction:2);

			// creates chorus effect by randomly detuning each tone
			20.do{
				temp = LFSaw.ar(
					freq * {Rand(0.98, 1.02)},
					{Rand(0.0, 0.1)},
					{ExpRand(0.005, 0.05)};
				);
				sum = sum + temp;
			};

			sig = Pan2.ar(sum, pan, amp);
			sig = RLPF.ar(sig, 8500, 0.05, 0.3);
			sig = sig * env * amp;

			Out.ar(0, sig);
		}).add;

		//____________________  III.

		SynthDef(\Special_CZ_Ř, {
			// not currently used in mapping
			// not deleted for context and consistency
		}).add;

		//____________________  IV.

		SynthDef(\Special_GE_ß, {
			// Not currently used in mapping
			// not deleted for context and consistency
		}).add;

		//____________________  V.

		SynthDef.new(\Nasals, {
			arg freq=440,
			sus=0.1, pan=0, ampl=0.1,
			freqFilOne=600, freqFilTwo=300;

			var temp, temp_two, sum, env, sig;
			env = EnvGen.ar(Env([0, 1, 1, 0], [0.1, sus, 0.1], [1, -1]), doneAction:2);

			sum = 0;

			// creates chorus effect by randomly detuning each tone
			30.do{
				temp_two = Blip.ar(freq * {Rand(0.97, 1.02)},
					6,
					1,
					1;
				);
				sum = sum + temp_two;
			};

			sum = sum * env;

			sig = RHPF.ar(sum, freqFilOne, 1, 1.5);
			sig = RLPF.ar(sig, freqFilTwo, 1, 1.5);
			sig = Pan2.ar(sig, pan, ampl);

			Out.ar(0, sig);
		}).add;

		//____________________  VI.

		SynthDef(\Fricatives, {
			arg atk=0.1, sus=0.1, rel=0,
			freqFilOne=77, freq2=10000, freq3=2000,
			cur1=1, cur2=(-1), pan=0, pantime=5, amp=5, pls=40, l=2;

			var sig, env;

			env = EnvGen.kr(Env([0, 1, 1, 0], [atk, sus, rel], [cur1, cur2]), doneAction:2);

			sig = PinkNoise.ar(0.6, 0.3) + WhiteNoise.ar(0.4, 0.1, 0.3);
			sig = sig * Resonz.ar(sig, 660, 10, 4);
			sig = Pan2.ar(sig, pan, 1);
			sig = RHPF.ar(sig, freqFilOne, 1);
			sig = RLPF.ar(sig, 1000, 1);
			sig = env * sig * amp;

			Out.ar(0, sig);
		}).add;

		//____________________  VII.

		SynthDef(\Plosives, {
			|buf=0, rate=1, amp=25, pan=0, amp2=1|
			var sig;

			sig = PlayBuf.ar(1, buf, BufRateScale.ir(buf) * rate, doneAction:2);
			sig = Pan2.ar(sig, pan, amp2);
			sig = sig * amp;

			Out.ar(0, sig);
		}).add;

		//____________________  VIII.

		SynthDef(\Affricates, {
			arg atk=0.1, sus=0.1, rele=0,
			freqFilOne=300, freqFilTwo=3000, freq2=10000, freq3=2000,
			cur1=1, cur2=(-1),
			pan=0, pantime=5, amp=5, pls=40, l=2;

			var sig, env;

			env = EnvGen.kr(Env([0, 1, 1, 0], [atk, sus, rele], [cur1, cur2]), doneAction:2);

			sig = PinkNoise.ar(0.4, 0.1, 0.3);
			sig = Pan2.ar(sig, pan, 1);
			sig = RHPF.ar(sig, XLine.kr(freqFilOne, freqFilTwo, 0.2));
			sig = RLPF.ar(sig, 1000, 1);
			sig = env * sig * amp;

			Out.ar(0, sig);
		}).add;

		//____________________  IX.

		SynthDef(\Approximantes, {
			|freq=440, width=0.5,
			atk=0.1, sus=0.1, rele=0.1,
			amp=0.3,
			freqFilOne=11000, freqFilTwo=440, filtTime=0.2, vibr=2,
			pan=0|

			var env, sig, temp, sum;

			sum = 0;

			env = Env.new([0, 1, 0.7, 0],[atk, sus, rele]);

			// creates chorus effect by randomly detuning each tone
			7.do{
				temp = Pulse.ar(freq * Rand(0.98, 1.01),
					width * Rand(0.99, 1.1), 0.8);
				sum = sum + temp;
			};

			sig = sum * EnvGen.kr(env, doneAction:2);
			sig = RLPF.ar(sig, Line.kr(freqFilOne, freqFilTwo, filtTime), 1);
			sig = Pan2.ar(sig, pan, amp);
			sig = sig * 0.1;

			Out.ar(0, sig);
		}).add;

		//____________________  X.

		SynthDef(\Vibrants, {
			arg atk=0.1, sus=0.1, rel=0,
			freq=1200, freq2=500, freq3=2000, cur1=1, cur2=(-1),
			pan=0, pantime=5, amp=5, pls=40, l=2;

			var sig, env;

			env = EnvGen.kr(Env([0, 1, 1, 0],[atk, sus, rel],[cur1, cur2]), doneAction:2);

			sig = PinkNoise.ar(0.6, 0.3) + WhiteNoise.ar(0.4, 0.1, 0.3);
			sig = sig * Resonz.ar(sig, 660, 10, 4);
			sig = sig * Pulse.kr(pls, 0.5);
			sig = Pan2.ar(sig, pan, 1);
			sig = RHPF.ar(sig, freq, 1);
			sig = RLPF.ar(sig, 1000, 1);
			sig = env * sig * amp * 2;

			Out.ar(0, sig);
		}).add;
		^[\Vocals_CZ, \Vocals_GE, \Nasals, \Fricatives, \Plosives, \Affricates, \Approximantes, \Vibrants]

	}

	// proxy patterns for left and right earcon for model/theoretical object sonification
	// side is reflexted in the name: freq -> freq_l (frequency left)

	// notice .play(quant: 0); --> methods are also players

	* leftEarcon {
		Pdef(\left_language, Pbind(
			\instrument, Pdefn(\instrument_l),
			\pan, Pdefn(\pan_l),
			\dur, Pdefn(\dur_l),
			\freq, Pdefn(\freq_l),
			\buf, Pdefn(\buf_l), // Number of buffer
			\freqFilOne, Pdefn(\freqFilOne_l),
			\freqFilTwo, Pdefn(\freqFilTwo_l),
			\rel, Pdefn(\rel_l),
			\sus, Pdefn(\sus_l),
			\width, Pdefn(\width_l),
			\amp, 1.3
		);
		).play(quant:0);
	}

	* rightEarcon {
		Pdef(\right_language, Pbind(
			\instrument, Pdefn(\instrument_r),
			\pan, Pdefn(\pan_r),
			\dur, Pdefn(\dur_r),
			\freq, Pdefn(\freq_r),
			\buf, Pdefn(\buf_r), // Number of buffer
			\freqFilOne, Pdefn(\freqFilTwo_r),
			\freqFilTwo, Pdefn(\freqFilOne_r),
			\rel, Pdefn(\rel_r),
			\sus, Pdefn(\sus_r),
			\width, Pdefn(\width_r),
			\amp, 1.3
		);
		).play(quant:0);
	}

	// proxies are defined with default values here
	// they are switched with switcher functions leftSetter/rightSetter/bothSetter

	leftEarconProxies {
		Pdefn(\instrument_l, \Vocals_CZ); // sound to be played
		Pdefn(\pan_l, 1); // panning -> side
		Pdefn(\dur_l, Pseq([1], 1)); // duration of tone
		Pdefn(\freq_l, Pseq([[80]], 1).midicps); // frequency, patterned
		Pdefn(\buf_l, 0); // number of buffer -> sample
		Pdefn(\freqFilOne_l, 0); // filter frequency A
		Pdefn(\freqFilTwo_l, 0); // filter frequency B
		Pdefn(\rel_l, 1); // envelope release
		Pdefn(\sus_l, 1); // envelope sustain
		Pdefn(\width_l, 0.02); // bandwidth in Pulse
	}

	// parameters same as in previous left earcon
	rightEarconProxies {
		Pdefn(\instrument_r, \Vocals_GE);
		Pdefn(\pan_r, -1);
		Pdefn(\dur_r, Pseq([1], 1));
		Pdefn(\freq_r, Pseq([[83]], 1).midicps);
		Pdefn(\buf_r, 0);
		Pdefn(\freqFilOne_r, 0);
		Pdefn(\freqFilTwo_r, 0);
		Pdefn(\rel_r, 1);
		Pdefn(\sus_r, 1);
		Pdefn(\width_r, 0.5);
	}

	// receives synth names, arguments and plays synths
	sentimentPlay {
		arg synthOne, synthTwo, argsOne, argsTwo;
		Synth(synthOne, argsOne);
		Synth(synthTwo, argsTwo);
	}

	// receives synth names, arguments and plays synths
	standardPlay {
		arg synthOne, synthTwo, argsOne, argsTwo;
		Synth(synthOne, argsOne);
		Synth(synthTwo, argsTwo);
	}

	// loads samples from library which are used by \plosives SynthDef
	plosivesBuffer {
		var audiopath, buff_array, buff_path, s;

		// defines paths
		audiopath = PathName(thisProcess.nowExecutingPath).parentPath;
		buff_array = Array.new;
		buff_path = PathName.new(audiopath ++ "Plosives/");

		// loads
		buff_path.entries.do({
			arg path;
			buff_array = buff_array.add(Buffer.read(s, path.fullPath))
		});
		^buff_array
	}
}