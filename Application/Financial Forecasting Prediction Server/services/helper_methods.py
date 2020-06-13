import datetime

import pandas as pd
import numpy as np

from ta import add_volume_ta, add_volatility_ta, add_trend_ta, add_momentum_ta, add_others_ta


def read_company_stock_data(stock_company_file_path: str) -> pd.DataFrame:
    stock_dataframe: pd.DataFrame = pd.read_csv(stock_company_file_path)
    return stock_dataframe


def rename_company_stock_columns(stock_dataframe: pd.DataFrame, company_ticker_symbol: str) -> pd.DataFrame:
    stock_dataframe.rename(columns=lambda name: company_ticker_symbol + "_" + name, inplace=True)
    return stock_dataframe


def drop_old_data(stock_dataframe: pd.DataFrame, starting_year: int, ending_year: int) -> pd.DataFrame:
    stock_dataframe = stock_dataframe[pd.to_datetime(stock_dataframe['Date']).dt.year >= starting_year]
    stock_dataframe = stock_dataframe[pd.to_datetime(stock_dataframe['Date']).dt.year <= ending_year]
    return stock_dataframe


def filter_data_between_dates(stock_dataframe: pd.DataFrame, starting_date: datetime.datetime,
                              ending_date: datetime.datetime) -> pd.DataFrame:
    stock_dataframe = stock_dataframe[pd.to_datetime(stock_dataframe['Date']) >= starting_date]
    stock_dataframe = stock_dataframe[pd.to_datetime(stock_dataframe['Date']) <= ending_date]
    return stock_dataframe


def add_timestamp_column(stock_dataframe: pd.DataFrame) -> pd.DataFrame:
    stock_dataframe['Timestamp'] = stock_dataframe['Date'].apply(lambda x: pd.to_datetime(x).timestamp()).astype(int)
    return stock_dataframe


def add_technical_indicators_volume(stock_dataframe: pd.DataFrame, open="Open", high="High", low="Low", close="Close",
                                    volume="Volume") -> pd.DataFrame:
    return add_volume_ta(df=stock_dataframe, high=high, low=low, close=close, volume=volume)


def add_technical_indicators_volatility(stock_dataframe: pd.DataFrame, open="Open", high="High", low="Low",
                                        close="Close", volume="Volume") -> pd.DataFrame:
    return add_volatility_ta(df=stock_dataframe, high=high, low=low, close=close)


def add_technical_indicators_trend(stock_dataframe: pd.DataFrame, open="Open", high="High", low="Low", close="Close",
                                   volume="Volume") -> pd.DataFrame:
    return add_trend_ta(df=stock_dataframe, high=high, low=low, close=close)


def add_technical_indicators_momentum(stock_dataframe: pd.DataFrame, open="Open", high="High", low="Low", close="Close",
                                      volume="Volume") -> pd.DataFrame:
    return add_momentum_ta(df=stock_dataframe, high=high, low=low, close=close, volume=volume)


def add_technical_indicators_others(stock_dataframe: pd.DataFrame, open="Open", high="High", low="Low", close="Close",
                                    volume="Volume") -> pd.DataFrame:
    return add_others_ta(df=stock_dataframe, close=close)


def add_technical_indicators(stock_dataframe: pd.DataFrame) -> pd.DataFrame:
    stock_dataframe = add_timestamp_column(stock_dataframe)

    stock_dataframe = add_technical_indicators_volume(stock_dataframe)
    stock_dataframe = add_technical_indicators_volatility(stock_dataframe)
    stock_dataframe = add_technical_indicators_trend(stock_dataframe)
    stock_dataframe = add_technical_indicators_momentum(stock_dataframe)
    stock_dataframe = add_technical_indicators_others(stock_dataframe)

    stock_dataframe.drop(columns="Timestamp", inplace=True)
    stock_dataframe.replace([np.inf, -np.inf], np.nan, inplace=True)
    stock_dataframe.fillna(0, inplace=True)
    return stock_dataframe


def prepare_company_stock_data(company_ticker_symbol: str,
                               company_csv_data_path: str,
                               starting_date: datetime.datetime,
                               ending_date: datetime.datetime) -> pd.DataFrame:
    stock_dataframe: pd.DataFrame = read_company_stock_data(company_csv_data_path)

    # drop data for preprocessing - only on training_stock_dataframe
    stock_dataframe = filter_data_between_dates(stock_dataframe, starting_date, ending_date)

    # use technical indicators
    stock_dataframe = add_technical_indicators(stock_dataframe)

    # rename columns
    stock_dataframe = rename_company_stock_columns(stock_dataframe, company_ticker_symbol)

    print(stock_dataframe.head())
    print(stock_dataframe.tail())
    return stock_dataframe


def read_singular_stock_index(stock_index_file_path: str) -> pd.DataFrame:
    stock_index_dataframe: pd.DataFrame = pd.read_csv(stock_index_file_path)
    return stock_index_dataframe


def rename_stock_index_columns(stock_dataframe: pd.DataFrame, stock_index_name: str) -> pd.DataFrame:
    stock_dataframe.rename(columns=lambda column_name: stock_index_name + "_" + column_name, inplace=True)
    return stock_dataframe


def preprocess_stock_indexes(stock_index_names_with_filepath,
                             starting_date: datetime.datetime,
                             ending_date: datetime.datetime) -> pd.DataFrame:
    stocks_indexes_dataframes = []
    for stock_index_name_with_filepath in stock_index_names_with_filepath:
        stock_index_name = stock_index_name_with_filepath[0]
        stock_index_filepath = stock_index_name_with_filepath[1]

        stock_index_dataframe = read_singular_stock_index(stock_index_filepath)

        # drop data for preprocessing - only on training_stock_index_dataframe
        stock_index_dataframe = filter_data_between_dates(stock_index_dataframe, starting_date, ending_date)

        # no technical indicators - for both
        # stock_index_dataframe = add_technical_indicators(stock_index_dataframe)

        # rename stock index columns
        stock_index_dataframe = rename_stock_index_columns(stock_index_dataframe, stock_index_name)

        print(stock_index_dataframe.head())
        print(stock_index_dataframe.tail())
        stocks_indexes_dataframes.append(stock_index_dataframe)

    reunited_dataframe = stocks_indexes_dataframes[0]
    for i in range(1, len(stock_index_names_with_filepath)):
        reunited_dataframe = reunited_dataframe.merge(right=stocks_indexes_dataframes[i], how='left',
                                                      left_on=stock_index_names_with_filepath[0][0] + "_Date",
                                                      right_on=stock_index_names_with_filepath[i][0] + "_Date")

    return reunited_dataframe


def link_company_with_indices_data(stock_company_dataframe: pd.DataFrame,
                                   stock_index_dataframe: pd.DataFrame,
                                   company_ticker_symbol: str,
                                   first_stock_index_name: str) -> pd.DataFrame:
    left_stock_company_column_name = company_ticker_symbol + "_Date"
    right_stock_company_column_name = first_stock_index_name + "_Date"

    unified_stock_dataframe = stock_company_dataframe.merge(right=stock_index_dataframe, how='left',
                                                            left_on=left_stock_company_column_name,
                                                            right_on=right_stock_company_column_name)
    unified_stock_dataframe_with_date = unified_stock_dataframe.copy(deep=True)
    return unified_stock_dataframe_with_date


def remove_date_columns_from_unified_dataframe(unified_stock_dataframe: pd.DataFrame,
                                               company_ticker_symbol: str,
                                               stock_index_names) -> pd.DataFrame:
    stock_company_column_name = company_ticker_symbol + "_Date"
    stock_indices_date_columns_names = []
    if len(stock_index_names) > 0:
        stock_indices_date_columns_names = [name + "_Date" for name in stock_index_names]

    unified_date_columns = [stock_company_column_name] + stock_indices_date_columns_names
    unified_stock_dataframe = unified_stock_dataframe.copy(deep=True)
    unified_stock_dataframe = unified_stock_dataframe.drop(columns=unified_date_columns)
    return unified_stock_dataframe.copy(deep=True)


def get_unified_stock_dataframe_with_dates(company_ticker_symbol: str,
                                           company_csv_data_path: str,
                                           stock_index_names_with_filepath,
                                           starting_date: datetime.datetime,
                                           ending_date: datetime.datetime) -> pd.DataFrame:
    stock_company_dataframe = prepare_company_stock_data(company_ticker_symbol, company_csv_data_path,
                                                         starting_date, ending_date)

    stock_index_dataframe = preprocess_stock_indexes(stock_index_names_with_filepath,
                                                     starting_date, ending_date)

    unified_stock_dataframe = link_company_with_indices_data(stock_company_dataframe,
                                                             stock_index_dataframe,
                                                             company_ticker_symbol,
                                                             stock_index_names_with_filepath[0][0])

    return unified_stock_dataframe


def get_dataframe_for_prediction(company_ticker_symbol: str,
                                 company_csv_data_path: str,
                                 stock_index_names_with_filepath,
                                 prediction_starting_date: str) -> pd.DataFrame:
    datetime_format: str = '%Y-%m-%d'
    prediction_starting_date_converted = datetime.datetime.strptime(prediction_starting_date, datetime_format)
    prediction_ending_date = prediction_starting_date_converted

    # we need to retrieve a the maximum of two dates for loading:
    # preprocessing: 2001-01-01 and (prediction_ending_date - 2years)
    preprocessing_starting_date = datetime.datetime(2001, 1, 1)
    two_years_before_prediction_date = prediction_starting_date_converted - datetime.timedelta(days=365 * 2)
    if two_years_before_prediction_date > preprocessing_starting_date:
        prediction_starting_date = two_years_before_prediction_date

    unified_stock_dataframe_with_dates = get_unified_stock_dataframe_with_dates(company_ticker_symbol,
                                           company_csv_data_path,
                                           stock_index_names_with_filepath,
                                           prediction_starting_date,
                                           prediction_ending_date)

    # at this point the latest N rows in our dataframe are necessary for the future prediction
    # we need to take them only and eliminate the date columns

    # take last N elements from dataframe
    past_history_no_days = 252
    reduced_stock_dataframe_with_dates = unified_stock_dataframe_with_dates.iloc[-past_history_no_days:]

    # eliminate all date columns and return the necessary information
    stock_index_names = \
        [stock_index_names_with_filepath[0] for stock_index_names_with_filepath in stock_index_names_with_filepath]
    reduced_stock_dataframe = remove_date_columns_from_unified_dataframe(
        reduced_stock_dataframe_with_dates,
        company_ticker_symbol,
        stock_index_names)

    returned_dataframe_copy = reduced_stock_dataframe.copy(deep=True)
    return returned_dataframe_copy


def standardize_sliding_window(unified_stock_dataset):
    dataset = unified_stock_dataset
    dataset_mean = dataset.mean(axis=0)
    dataset_std = dataset.std(axis=0)
    dataset_std += 1e-6  # avoid nans

    standardized_dataset = (dataset - dataset_mean) / dataset_std
    # 3 is the Close column
    close_dataset_mean = dataset_mean[3]
    close_dataset_std = dataset_std[3]
    return [standardized_dataset, close_dataset_mean, close_dataset_std]


def normalize_sliding_window(unified_stock_dataset):
    dataset = unified_stock_dataset

    dataset_max = dataset.max(axis=0)
    dataset_min = dataset.min(axis=0)
    dataset_difference = dataset_max - dataset_min
    dataset_difference += 1e-6  # avoid nans

    normalized_dataset = (dataset - dataset_min) / dataset_difference
    # 3 is the Close column
    close_dataset_min = dataset_min[3]
    close_dataset_minmax_difference = dataset_difference[3]
    return [normalized_dataset, close_dataset_min, close_dataset_minmax_difference]


def normalize_increased_interval_sliding_window(unified_stock_dataset):
    dataset = unified_stock_dataset

    dataset_max = dataset.max(axis=0) * 1.2
    dataset_min = dataset.min(axis=0) * 0.8
    dataset_difference = dataset_max - dataset_min
    dataset_difference += 1e-6  # avoid nans

    normalized_dataset = (dataset - dataset_min) / dataset_difference
    # 3 is the Close column
    close_dataset_min = dataset_min[3]
    close_dataset_minmax_difference = dataset_difference[3]
    return [normalized_dataset, close_dataset_min, close_dataset_minmax_difference]

