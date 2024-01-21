{
    # Using gsub to replace all occurrences of "apple" with "banana"
    gsub("apple", "banana")
    print "Using gsub:", $0
    
    # Resetting the string
    $0 = "apple orange apple"
    
    # Using sub to replace the first occurrence of "apple" with "banana"
    sub("apple", "banana")
    print "Using sub:", $0
}