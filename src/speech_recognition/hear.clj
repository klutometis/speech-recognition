(ns speech-recognition.hear
  (:use [clojure.data.json :as json]
        [clj-http.client :as http])
  (:import (java.io File
                    FileOutputStream)
           (javax.sound.sampled AudioFormat
                                AudioSystem
                                AudioInputStream
                                AudioFileFormat
                                AudioFileFormat$Type)
           (javaFlacEncoder FLAC_FileEncoder
                            StreamConfiguration)))

(def ^:dynamic *google-url*
  "https://www.google.com/speech-api/v1/recognize?xjerr=1&client=chromium&lang=en-US")

(def ^:dynamic *input-index* 
  "Default index of the recording device; NB: this is a hack."
  1)

(def ^:dynamic *sample-rate* 8000)

(def ^:dynamic *sample-size* 16)

(def ^:dynamic *channels* 1)

(def ^:dynamic *signed* true)

(def ^:dynamic *big-endian* false)

(def ^:dynamic *format*
  (new AudioFormat
       *sample-rate*
       *sample-size*
       *channels*
       *signed*
       *big-endian*))

(def ^:dynamic *prefix* "iris")

(def create-temporary-file
  (fn [suffix] (File/createTempFile *prefix* suffix)))

(def create-temporary-wave
  (fn [] (create-temporary-file ".wav")))

(def create-temporary-flac
  (fn [] (create-temporary-file ".flac")))

(def post-to-google
  (fn [flac]
     (:body
      (http/post
       *google-url*
       {:multipart [["Content" flac]]
        :headers {"Content-type"
                  (format "audio/x-flac; rate=%s" *sample-rate*)}}))))

(def sort-hypotheses
  (fn [hypotheses]
     (sort-by (fn [hypothesis]
                 (let [{utterance :utterance confidence :confidence}
                       hypothesis]
                   confidence))
              >
              hypotheses)))

(def parse-response
  (fn [response]
     (let [{status :status
            id :id
            hypotheses :hypotheses}
           (json/read-json response)
           {utterance :utterance
            confidence :confidence}
           (first (sort-hypotheses hypotheses))]
       utterance)))

(defn hear []
  (let [mixer-info (clojure.core/get (AudioSystem/getMixerInfo) *input-index*)
        target (AudioSystem/getTargetDataLine *format* mixer-info)]
    ;; `with-open'?
    (.open target *format*)
    (println "I'm listening.")
    (.start target)
    (.start (Thread.
             (fn []
                (read-line)
                (.flush target)
                (.stop target)
                (.close target)
                (println "I'm considering."))))
    (let [input-stream (new AudioInputStream target)]
      (let [wave (create-temporary-wave)
            flac (create-temporary-flac)]
        (AudioSystem/write input-stream
                           AudioFileFormat$Type/WAVE
                           wave)
        (let [encoder (new FLAC_FileEncoder)]
          (.setStreamConfig encoder
                            (new StreamConfiguration
                                 *channels*
                                 StreamConfiguration/DEFAULT_MIN_BLOCK_SIZE
                                 StreamConfiguration/DEFAULT_MAX_BLOCK_SIZE
                                 *sample-rate*
                                 *sample-size*))
          (.encode encoder wave flac)
          (parse-response (post-to-google flac)))))))
