# DirWalker

Walks through a directory and provides statistics regarding file/dir size. It provides a linux tree-like command line output as well as a web server for presenting the information in web pages and programming interfaces in JSON/XML.

## Usage
1. Command line

        lein run --dir PATH_TO_DIR
2. Run as a web server (default port 8080)

        lein run --server --dir PATH_TO_DIR [--port PORT_NUM]       
3. Print help options

        lein run -m dirwalker.cmdmain -h

## Features
1. calculate total size of a dir
2. calculate total number of files
3. file filtering (if only *.txt) (@todo filter by size, e.g. only size > 1000)
4. aggregate a tree-like piece of information as a string (like tree command, but with size/number attached)
 - provides a map of info about all nodes (dir and sub dirs)
   { :name "test-folder"
     :size 5000
     :count 5
     :sub-info [
       { :name "folder1"
         :size 2000
         :count 2
         :sub-info []     ;; no sub folder in folder1
       }
       { :name "folder2"
         :size 2000
         :count 2
         :sub-info [
           { :name "folder2.1" 
             :size 1000
             :count 1
             :sub-info []
           }
           { :name "folder2.2" 
             :size 1000
             :count 1
             :sub-info []
           }
         ]
       }
     ]
   }
 - use this map to construct a tree-like piece of info 
5. print like the tree command, with total size and file count info, and proportion in size
6. util & facade (for web UI building, for command line building)
6. web UI
7. REST (JSON/XML)
8. parallelize the traversing of dir (doesn't appear to improve performance as it is probably IO related) 
9. use icons in html display
10. make lists/table sortable (server side sort) in html display
11. add last modified date (and make it sortable)
12. caching a dir info
13. @todo refactor (parameter passing, etc) 
14. @todo testing in clojure (http testing?)

## License

Copyright © 2014

Distributed under the Eclipse Public License, the same as Clojure.
