BEGIN {
    # String functions
    str = "Hello, World!"
    print "String length:", length(str)
    print "Uppercase:", toupper(str)
    print "Lowercase:", tolower(str)
    print "Substring:", substr(str, 1, 5)
    
    # Array functions
    split("one two three", arr)
    print "Array elements:"
    for (i in arr) {
        print arr[i]
    }
    
    # Input functions
    print "Enter a number:"
    getline userInput
    print "You entered:", userInput
    
    # Output functions
    printf "Formatted output: %s %.2f\n", str, num
}