# audio-fingerprint

Landmark based audio fingerprinting.

P.o.C. implementation of [An Industrial-Strength Audio Search Algorithm](https://www.ee.columbia.edu/~dpwe/papers/Wang03-shazam.pdf).


## Intention

I have a huge set of badly labeled, short music mixes consisting of just two tracks and a transition between them. 
Most time only a few seconds can be heard from the first track before the next track starts to play. 
There might also be loops, scratches and effects involved not heard in the original track.  My goal is to extract both 
the source and target track and tag the mix appropriately. 

I'm basically following the same approach as described in the paper. But even though the paper states good information 
about how the algorithm works, it lacks on the actual parameter values which highly influence it's success rate. These
values might depend on genre and even the DJ and his mixing style (e.g. long transitions or short cuts, effects, 
scratches, tone play, drumming, ..). 

My best run led to a ~90% success rate on ~200 mixes of ~1000 different tracks.

## Workflow

There are 3 different tasks which can be done with this project. It either imports songs into the DB, matches a song 
against the DB or renders sexy images with the spectrum, peaks and hashes visualized. The whole project is built around 
unit tests since managing a runnable jar with native dependencies is a mess and.. it's still a P.o.C. 

The mp3-conversion and all DSP-tasks are realized with @hendricks73's great libraries:
 - [Jipes](https://github.com/hendriks73/jipes) for all the DSP tasks 
 - [mfsampledsp](https://github.com/hendriks73/mfsampledsp) for mp3 conversion on windows
 - [casampledsp](https://github.com/hendriks73/casampledsp) on mac

I suggest to check out their documentation before reading the source, it is very well documented and makes things 
more clear. The underlying database engine is [H2](https://github.com/h2database).

The workflow is split up into different signal processors from Jipes. 

> [Jipes](http://www.tagtraum.com/jipes/) is an open source library that allows you to efficiently compute audio features.
> In Jipes, signal processors connected to each other, form so called pipelines. 
> It lets you create pipelines you can handle just like a regular, single signal processor (Composite pattern). 
> In the end, features are nothing else but the product of a pipeline.

`de.mknblch.audiofp.PipeBuilder` builds such a pipeline which is later used to transform the audio into hashes. The 
Pipeline then gets expanded with either:
 - a `de.mknblch.audiofp.db.DBWriter` to write the generated hashes into the DB
 - a `de.mknblch.audiofp.db.DBFinder` to match a song against the DB 
 - or a `de.mknblch.audiofp.processor.ImageAggregator` to render sexy images
 
### Transformation Pipeline

Before feeding the audio data into the pipe it gets unified using `de.mknblch.audiofp.AudioSource` to a constant 
format. The pipe is built and set up in `de.mknblch.audiofp.PipeBuilder` and follows these steps:

- it first converts the stereo input signal into mono data
- next step is to downsample the signal which limits the bandwidth but I don't care about high 
frequencies anyway
- the signal then gets transformed into frequency domain with a sliding window and a window function using the FFT
- the resulting `com.tagtraum.jipes.audio.AudioSpectrum` gets cut to remove low and high frequencies depending on 
thresholds
- from the AudioSpectrum the magnitude gets calculated and scaled (for visualization)
- since finding local peaks in the spectrum tends to be very sensitive I'm using a `Whitening` processor which just kills
the signal based on a threshold. The threshold itself is calculated using the peaks from previous frames over a given 
window size.
- to enrich the AudioSpectrum with feature data it gets wrapped into a `de.mknblch.audiofp.Feature`
- now the `de.mknblch.audiofp.processor.LocalMaximum` processor adds the peaks to the feature dto by aggregating
several frames in a queue for a given time (or frame count) before sending it to the next processor. Those peaks 
contain frequency-value-pairs and are laid out in descending order (based on the value).
- at this point we are ready to calculate the hashes. This is done by `de.mknblch.audiofp.processor.Fingerprint` 
which resembles the logic from the paper by furthermore spanning a window over the peaks of several frames and 
transforming them into `de.mknblch.audiofp.Hash` dto's.

The resulting pipe emits a stream of features with the spectrum, the peak bins and values and the list
of hashes.

### Import

The import process is shown in `de.mknblch.audiofp.ImportTracksTest`. It basically scans a given directory for
tracks with a matching file suffix and transforms them into hashes. Those hashes are then written into the DB including 
a unique id for the song and the timestamp of their occurrence.

### Matching

`de.mknblch.audiofp.MatchingTest` shows the matching process. It uses a `de.mknblch.audiofp.common.TimestampSignalSource`
to transform short parts of the given file into hashes and matches them against the DB. I tried to keep the matching
logic as simple as possible. Unfortunately just taking the count of matching hashes does not work with 1000 songs 
anymore. To evaluate which track matches best against a list of hashes I just take the list of corresponding timestamps 
and calculate their frequency in a hardcoded time raster and it's maximum value is the final score of a track. 
This approach is far from optimal but led to good results on my data set.

### Image rendering

I found it very amazing to see what different configurations do to the hashes. So I built a test 
(`de.mknblch.audiofp.ShowTrackTest`) to visualize them. It renders an audio file into a BufferedImage and displays it
for a short time. The resulting image shows the frequency intensity, the audio peaks as red dots and the hashes as 
yellow lines.

## TODO

- find / create legal test data
- add mac profile & dependencies
- readme & images



