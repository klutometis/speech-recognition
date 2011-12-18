# speech-recognition

Library to listen to audio input and interpret it to text.

# Usage

Add this to your project.clj:
   
    [facts/speech-recognition "1.0.0"]

Add to your ns:

    (:use [speech-recognition.hear :as hear])

Turn speech to text:

    (hear/hear)
