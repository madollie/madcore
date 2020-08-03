def play_sound(self, frequency):
	pipeline = Gst.Pipeline(name='note')
	source = Gst.ElementFactory.make('audiotestsrc', 'src')
	sink = Gst.ElementFactory.make('autoaudiosink', 'output')

	source.set_property('freq', frequency)
	pipeline.add(source)
	pipeline.add(sink)
	source.link(sink)
	pipeline.set_state(Gst.State.PLAYING)

	GObject.timeout_add(self.LENGTH, self.pipeline_stop, pipeline)
