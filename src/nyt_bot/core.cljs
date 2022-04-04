(ns nyt-bot.core
  (:require-macros [cljs.core.async.macros :refer [go]])
    (:require [reagent.core :as reagent]
    [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
      ))

(enable-console-print!)

(defonce firstArticle (reagent/atom {:headline "Sample Headline"
                          :author "Sample author"
                          :snippet "Sample snip"
                          :description "Sample desc"
                          :url "https://www.nytimes.com"}))

;; atoms are concurrent, and are an example of concurrent programming in Clojure. They let you store variables
(def nyt-url "https://api.nytimes.com/svc/search/v2/articlesearch.json?")
(def api-response (reagent/atom ""))
(def searchString (reagent/atom ""))
(def section (reagent/atom ""))
(def startDate (reagent/atom nil))
(def endDate (reagent/atom nil))
(def currentArticleNum (reagent/atom 0))

(defn formatDate [date]
  (clojure.string/replace date #"-" "")
  )

(defn showArticle [num]
            (swap! firstArticle assoc :snippet (:snippet (nth @api-response num)))
            (swap! firstArticle assoc :description (:lead_paragraph (nth @api-response num)))
            (swap! firstArticle assoc :headline (:main (:headline (nth @api-response num))))
            (swap! firstArticle assoc :author (:original (:byline (nth @api-response num))))
            (swap! firstArticle assoc :url (:web_url (nth @api-response num)))

  )

(defn article-search-by-date
  [query section start-date end-date]

  (go
    (let [response (<! (http/get (str nyt-url
                          "q=" query
                          "&news_desk=" section
                            "&begin_date=" start-date
                            "&end_date=" end-date
                            "&api-key=gKZ1J7brRHlGLtWKdbBB2OGZzjGEZFyG")
                                 {:with-credentials? false}))]

            (reset! api-response (:docs (:response (:body response))))
          (showArticle 1)
    ))
)

(defn inputQuery [value]
  [:input {:type "text"
           :value @value
           :on-change #(reset! value (-> % .-target .-value))}])

(defn inputSection [value]
  [:input {:type "text"
           :value @value
           :on-change #(reset! value (-> % .-target .-value))}])

(defn inputStartDate [value]
  [:input {:type "date"
           :value @value
           :on-change #(reset! value (-> % .-target .-value))}])

(defn inputEndDate [value]
  [:input {:type "date"
           :value @value
           :on-change #(reset! value (-> % .-target .-value))}])


(defn getArticleButton []
  [:input {:type "button"
           :value "Get Articles"
           :on-click #(article-search-by-date @searchString @section (formatDate @startDate) (formatDate @endDate))}])

(defn openInBrowserButton []
  [:input {:type "button"
           :value "Open URL"
        
           :on-click #(js/window.open (:url @firstArticle))}])

(defn previousArticleButton []
  [:input {:type "button"
           :value "Previous Article"

           :on-click (fn [] (
             (reset! currentArticleNum (- @currentArticleNum 1))
              (showArticle @currentArticleNum)
           ))
           }])

(defn nextArticleButton []
  [:input {:type "button"
           :value "Next Article"

           :on-click (fn []
           (
             (reset! currentArticleNum (+ 1 @currentArticleNum))
              (showArticle @currentArticleNum)
           ))
           }])

(defn render-page [state]
      [:div.page
   [:h1 "NYT Bot"]
  [:div#queryDetails
   [:h3 "Query " [inputQuery searchString]]
  [:h3 "Section " [inputSection section]]
   [:h3 "Start Date "  [inputStartDate startDate]]
   [:h3 "End Date "  [inputEndDate endDate]]
   [:h3 ""  [ getArticleButton]]]
   
   [:div#articleDetails
      [:h3.articleTitle (:headline @state)]
      [:h3.articleAuthor (:author @state)]
      [:h3.articleSnippet (:snippet @state)]
      [:h3.articleLeadParagraph (:description @state)]
    [:div#optionButtons 
      [:h3 [ previousArticleButton]]
      [:h3 [ openInBrowserButton]]
      [:h3 [ nextArticleButton]]
    ]
    ]
   ]
   )

(reagent/render-component [render-page firstArticle]
                          (. js/document (getElementById "app")))
