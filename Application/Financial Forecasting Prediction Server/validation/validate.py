import datetime


def validate_non_empty_string(string: str, string_name: str):
    if not string:
        raise Exception("Empty string {} was received".format(string_name))


def validate_date_string_format(string: str):
    datetime_format: str = '%Y-%m-%d'
    try:
        prediction_starting_date_converted = datetime.datetime.strptime(string, datetime_format)
    except ValueError:
        raise Exception("String received {} was not respecting the date format".format(string))