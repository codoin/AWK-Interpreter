BEGIN {FS=":"; print "---header---"} { print $1; print length($1) } END {print "---footer---"}
