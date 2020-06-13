import os

import numpy as np
import tensorflow as tf

from services.helper_methods import get_dataframe_for_prediction, standardize_sliding_window, \
    normalize_increased_interval_sliding_window
from validation.validate import validate_non_empty_string, validate_date_string_format


def predict_service(company_ticker_symbol: str,
                    company_csv_data_path: str,
                    prediction_starting_date: str,
                    dow_jones_industrial_csv_data_path: str,
                    nasdaq_composite_csv_data_path: str) -> [str, float]:
    validate_parameters_received(company_ticker_symbol, company_csv_data_path, prediction_starting_date,
                                 dow_jones_industrial_csv_data_path, nasdaq_composite_csv_data_path)
    stock_index_names_with_filepath = get_stock_index_names_with_filepath(dow_jones_industrial_csv_data_path,
                                                                          nasdaq_composite_csv_data_path)

    reduced_stock_dataframe = get_dataframe_for_prediction(company_ticker_symbol,
                                                           company_csv_data_path,
                                                           stock_index_names_with_filepath,
                                                           prediction_starting_date
                                                           )

    input_values = reduced_stock_dataframe.to_numpy()
    input_values = np.nan_to_num(input_values)

    classification_result = classification_prediction(input_values,
                                                      company_ticker_symbol)
    regression_result = regression_prediction(input_values,
                                              company_ticker_symbol)

    return [classification_result, regression_result]


def classification_prediction(input_values,
                              company_ticker_symbol: str) -> str:
    # standardize input_values
    [standardized_input_values, _, _] = standardize_sliding_window(input_values)

    # load good model
    best_model = None
    default_model_path = "saved_models/classification_model"

    if os.path.exists("saved_models/{}_classification_model".format(company_ticker_symbol)):
        best_model = tf.keras.models.load_model("saved_models/{}_classification_model".format(company_ticker_symbol))
    else:
        best_model = tf.keras.models.load_model(default_model_path)
    print(best_model.summary())

    # predict
    prediction_result = np.argmax(best_model.predict(np.expand_dims(standardized_input_values, axis=0))[0])

    # return result
    if prediction_result == 0:
        return "Decrease"
    else:
        return "Increase"


def regression_prediction(input_values,
                          company_ticker_symbol: str) -> float:
    # normalize input_values
    [normalized_input_values, close_min, close_minmax_difference] = normalize_increased_interval_sliding_window(input_values)

    # load good model
    best_model = None
    default_model_path = "saved_models/regression_model"

    if os.path.exists("saved_models/{}_classification_model".format(company_ticker_symbol)):
        best_model = tf.keras.models.load_model("saved_models/{}_regression_model".format(company_ticker_symbol))
    else:
        best_model = tf.keras.models.load_model(default_model_path)
    print(best_model.summary())

    # predict
    prediction_result = best_model.predict(np.expand_dims(normalized_input_values, axis=0))[0][0]
    prediction_result = prediction_result * close_minmax_difference + close_min

    # return prediction result
    return prediction_result


def validate_parameters_received(company_ticker_symbol: str,
                                 company_csv_data_path: str,
                                 prediction_starting_date: str,
                                 dow_jones_industrial_csv_data_path: str,
                                 nasdaq_composite_csv_data_path: str):
    validate_non_empty_string(company_ticker_symbol, "Company Ticker Symbol")
    validate_non_empty_string(company_csv_data_path, "Company CSV Data Path")

    validate_date_string_format(prediction_starting_date)

    validate_non_empty_string(dow_jones_industrial_csv_data_path, "DowJonesIndustrial CSV Data Path")
    validate_non_empty_string(nasdaq_composite_csv_data_path, "NasdaqComposite CSV Data Path")


def get_stock_index_names_with_filepath(dow_jones_industrial_csv_data_path: str,
                                        nasdaq_composite_csv_data_path: str):
    return [("DJI", dow_jones_industrial_csv_data_path),
            ("IXIC", nasdaq_composite_csv_data_path)]
