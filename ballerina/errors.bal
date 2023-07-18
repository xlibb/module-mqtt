# The common error type for the module.
public type Error distinct error<ErrorDetails>;

# The error details type for the module.
#
# + reasonCode - The reason code for the error
public type ErrorDetails record {|
    int reasonCode?;
|};
