{
    # Using match to find the position of the word "fox" in the string
    if (match($0, `fox`)) {
        print "Found 'fox'"
    } else {
        print "No match found for 'fox'"
    }
    
    # Using match to find the position of the word "elephant" in the string
    if (match($0, `elephant`)) {
        print "Found 'elephant' at position:", RSTART
    } else {
        print "No match found for 'elephant'"
    }
}