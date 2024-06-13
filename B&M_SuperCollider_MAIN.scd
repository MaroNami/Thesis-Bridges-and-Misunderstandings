(
s.waitForBoot(
	~receive_OSC = BMReceive.new;
	~connection = BMReceive.connect("127.0.0.1", 57120);

	~sent = BMSynthetizer.sentimentSynths;
	~audi = BMSynthetizer.standardSynths;
	~art = BMSynthetizer.artSynths;
	~buffer = BMSynthetizer.new.plosivesBuffer;
	~le = BMSynthetizer.new.leftEarconProxies;
	~re = BMSynthetizer.new.rightEarconProxies;


	~sentiment = [
		['\sentiment_left',
			'/sentiment_analysis_left',
			'Left',
			~sent],
		['\sentiment_right',
			'/sentiment_analysis_right',
			'Right',
			~sent]
	];

	~art = [
		['\receiver_left',
			'/left',
			0,
			1,
			0.2,
			'art'],
		['\receiver_right',
			'/right',
			1,
			-1,
			0.2,
			'art'],
		['\receiver_both',
			'/both',
			2,
			[1, -1],
			0.2,
			'art']
	];

	~model = [
		['\receiver_left_model',
			'/left_model',
			0,
			1,
			0.2,
			'model'
		],
		['\receiver_right_model',
			'/right_model',
			1,
			-1,
			0.2,
			'model']
	];

	~standard = [
		['\modeone_L',
			'/audi_left',
			0,
			0,
			0.2,
			~audi[0]],
		['\modeone_R',
			'/audi_right',
			0,
			1,
			0.2,
			~audi[1]],
		['\modetwo_L',
			'/earcons_left',
			1,
			0,
			0.2,
			~audi[0],
			[96, 100, 103]],
		['\modetwo_R',
			'/earcons_right',
			1,
			1,
			0.2,
			~audi[1],
			[96, 100, 103]]
	];

	~artistic_sonification = ~receive_OSC.artisticBM(~art);
	~model_sonificaiton = ~receive_OSC.artisticBM(~model);
	~standard_sonification = ~receive_OSC.standardBM(~standard);
	~sentimentAnalysis = ~receive_OSC.sentimentAnalysis(~sentiment);
)
)
s.quit;

s.prepareForRecord;

s.record;
s.stopRecording;

s.delay = 0;

