(defproject facts/speech-recognition "1.0.0"
  :description "Library to listen to audio input and interpret it to text."
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [net.sourceforge.javaflacencoder/java-flac-encoder "0.2.3"]
                 [clj-http "0.2.5"]
                 [fs "1.0.0"]
                 [org.clojure/data.json "0.1.1"]]
  :dev-dependencies [[swank-clojure "1.4.0-SNAPSHOT"]
                     [lein-clojars "0.6.0"]]
  :repositories {"conjars" "http://conjars.org/repo/"}
)
