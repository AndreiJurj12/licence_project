from __future__ import absolute_import, division, print_function, unicode_literals

import math
import os

import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
import tensorflow as tf
from absl import app, flags, logging
from absl.flags import FLAGS
from ta import add_all_ta_features

flags.DEFINE_string('company_ticker_symbol', 'jpm', 'shortcut name of the company')
flags.DEFINE_string('stocks_path', '../Data/Kaggle_Stocks/Stocks', 'path for the stock information about the company')
flags.DEFINE_string('company_file_ending', '.us.txt', 'ending name for the file containing stock information')

flags.DEFINE_list('removed_columns_stock_company', 'OpenInt', 'list of columns to remove for stock company dataframe')

flags.DEFINE_boolean('use_technical_indicators', 0, 'whether to add technical indicators for training or not')

flags.DEFINE_boolean('use_stock_indices', 1, 'whether to use or not stock indices for the network')
flags.DEFINE_list('stock_indices_list', 'S&P500, DowJonesIndustrial, NasdaqComposite', 'names of stock indices used')
# flags.DEFINE_list('stock_indices_list', 'S&P500', 'names of stock indices used')

flags.DEFINE_enum('normalize_data', 'increased_min_max', ['none', 'min_max', 'increased_min_max'],
                  'whether to normalize data or not')
flags.DEFINE_boolean('standardize_data', 1, 'whether to standardize data or not')

flags.DEFINE_integer('percentage_split', 80, 'Percentage of training vs validation data')
flags.DEFINE_string('output_directory', 'output', 'Output directory for the plots')
flags.DEFINE_string('checkpoints_directory', 'checkpoints', 'Output directory for the checkpoints weights')

flags.DEFINE_integer('past_history_no_days', 30, 'The no days to use for past history data')
flags.DEFINE_integer('future_prediction_no_days', 3, 'The no days to use for predicting in the future')

flags.DEFINE_integer('batch_size', 128, 'batch size for training')
flags.DEFINE_integer('no_epochs', 2, 'no epochs for training')


def read_company_stock_data(company_ticker_symbol: str) -> pd.DataFrame:
    stocks_company_filepath = os.path.join(FLAGS.stocks_path, company_ticker_symbol + FLAGS.company_file_ending)
    stock_dataframe: pd.DataFrame = pd.read_csv(stocks_company_filepath)
    return stock_dataframe


def remove_unnecessary_company_stock_columns(stock_dataframe: pd.DataFrame) -> pd.DataFrame:
    logging.info("Columns to be removed from company stock data: " + str(FLAGS.removed_columns_stock_company))

    stock_dataframe.drop(columns=FLAGS.removed_columns_stock_company, inplace=True)
    return stock_dataframe


def rename_company_stock_columns(stock_dataframe: pd.DataFrame, company_ticker_symbol: str) -> pd.DataFrame:
    stock_dataframe.rename(columns=lambda name: company_ticker_symbol + "_" + name, inplace=True)
    return stock_dataframe


def drop_very_old_data(stock_dataframe: pd.DataFrame) -> pd.DataFrame:
    stock_dataframe = stock_dataframe[pd.to_datetime(stock_dataframe['Date']).dt.year >= 1990]
    stock_dataframe = stock_dataframe[pd.to_datetime(stock_dataframe['Date']).dt.year <= 2005]
    # stock_dataframe = stock_dataframe[pd.to_datetime(stock_dataframe['Date']).dt.year >= 1990]
    return stock_dataframe


def add_timestamp_column(stock_dataframe: pd.DataFrame) -> pd.DataFrame:
    stock_dataframe['Timestamp'] = stock_dataframe['Date'].apply(lambda x: pd.to_datetime(x).timestamp()).astype(int)
    return stock_dataframe


def prepare_company_stock_data(company_ticker_symbol: str) -> pd.DataFrame:
    stock_dataframe: pd.DataFrame = read_company_stock_data(company_ticker_symbol)
    stock_dataframe = remove_unnecessary_company_stock_columns(stock_dataframe)
    stock_dataframe = drop_very_old_data(stock_dataframe)
    if FLAGS.use_technical_indicators:
        stock_dataframe = add_timestamp_column(stock_dataframe)
        stock_dataframe = add_all_ta_features(df=stock_dataframe, open="Open", high="High", low="Low", close="Close",
                                              volume="Volume")
        stock_dataframe.drop(columns="Timestamp", inplace=True)
        stock_dataframe.fillna(0, inplace=True)
    stock_dataframe = rename_company_stock_columns(stock_dataframe, company_ticker_symbol)

    print(stock_dataframe.head())
    print(stock_dataframe.tail())
    return stock_dataframe


def read_singular_stock_index(filename: str) -> pd.DataFrame:
    parent_path = ".."
    stock_index_path = os.path.join(parent_path, filename)
    stock_index_dataframe: pd.DataFrame = pd.read_csv(stock_index_path)
    return stock_index_dataframe


def rename_stock_index_columns(stock_dataframe: pd.DataFrame, stock_index_name: str) -> pd.DataFrame:
    stock_dataframe.rename(columns=lambda column_name: stock_index_name + "_" + column_name, inplace=True)
    return stock_dataframe


def preprocess_stock_indexes() -> pd.DataFrame:
    stocks_indexes_dataframes_columns = FLAGS.stock_indices_list
    filenames_list = [name + '.csv' for name in stocks_indexes_dataframes_columns]
    stocks_indexes_dataframes = []

    for filename in filenames_list:
        stock_index_dataframe = read_singular_stock_index(filename)
        stock_index_dataframe = drop_very_old_data(stock_index_dataframe)
        # here for the filename used for renaming - get string partition before substring
        stock_index_dataframe = rename_stock_index_columns(stock_index_dataframe, filename.partition('.csv')[0])

        print(stock_index_dataframe.head())
        print(stock_index_dataframe.tail())
        stocks_indexes_dataframes.append(stock_index_dataframe)

    reunited_dataframe = stocks_indexes_dataframes[0]
    for i in range(1, len(stocks_indexes_dataframes_columns)):
        reunited_dataframe = reunited_dataframe.merge(right=stocks_indexes_dataframes[i], how='left',
                                                      left_on=stocks_indexes_dataframes_columns[0] + "_Date",
                                                      right_on=stocks_indexes_dataframes_columns[i] + "_Date")

    return reunited_dataframe


def link_company_with_indices_data(stock_company_dataframe: pd.DataFrame,
                                   stock_index_dataframe: pd.DataFrame) -> pd.DataFrame:
    left_stock_company_column_name = FLAGS.company_ticker_symbol + "_Date"
    right_stock_company_column_name = FLAGS.stock_indices_list[0] + "_Date"

    unified_stock_dataframe = stock_company_dataframe.merge(right=stock_index_dataframe, how='left',
                                                            left_on=left_stock_company_column_name,
                                                            right_on=right_stock_company_column_name)
    unified_stock_dataframe_with_date = unified_stock_dataframe.copy(deep=True)
    return unified_stock_dataframe_with_date


def remove_date_columns_from_unified_dataframe(unified_stock_dataframe: pd.DataFrame) -> pd.DataFrame:
    stock_company_column_name = FLAGS.company_ticker_symbol + "_Date"
    stock_indices_date_columns_names = []
    if FLAGS.use_stock_indices:
        stock_indices_date_columns_names = [name + "_Date" for name in FLAGS.stock_indices_list]


    unified_date_columns = [stock_company_column_name] + stock_indices_date_columns_names
    unified_stock_dataframe.drop(columns=unified_date_columns, inplace=True)
    return unified_stock_dataframe


"""
    Returns the ending index for the dataset, such that dataset[0:index] represents
    percentage_split % of the entire dataset
"""


def compute_training_split_index(dataset: pd.DataFrame, percentage_split=80) -> int:
    number_rows = dataset.shape[0]
    return (number_rows * percentage_split) // 100


def standardize_data(unified_stock_dataset, training_ending_index):
    dataset = unified_stock_dataset
    dataset_mean = dataset[:training_ending_index].mean(axis=0)
    dataset_std = dataset[:training_ending_index].std(axis=0)

    standardized_dataset = (dataset - dataset_mean) / dataset_std
    return standardized_dataset


def normalize_data(unified_stock_dataset, training_ending_index):
    dataset = unified_stock_dataset
    dataset_max = dataset[:training_ending_index].max(axis=0)
    dataset_min = dataset[:training_ending_index].min(axis=0)

    normalized_dataset = (dataset - dataset_min) / (dataset_max - dataset_min)
    return normalized_dataset


def normalize_data_increased_interval(unified_stock_dataset, training_ending_index):
    dataset = unified_stock_dataset
    dataset_max = dataset[:training_ending_index].max(axis=0)
    dataset_max = dataset_max * 1.5
    dataset_min = dataset[:training_ending_index].min(axis=0)
    dataset_min = dataset_min * 0.66

    normalized_dataset = (dataset - dataset_min) / (dataset_max - dataset_min)
    return normalized_dataset


def get_real_price(x, close_min, close_max, close_mean, close_std):
    x = (np.asarray(x) * close_std + close_mean) * (close_max - close_min) + close_min
    return x


def plot_company_close_price(unified_stock_dataframe_with_date: pd.DataFrame, normalized_dataset):
    plt.figure(figsize=(18, 9))
    plt.plot(range(normalized_dataset.shape[0]), normalized_dataset[:, 3])
    plt.xticks(range(0, normalized_dataset.shape[0], 500),
               unified_stock_dataframe_with_date[FLAGS.company_ticker_symbol + '_Date'].loc[::500], rotation=45)
    plt.xlabel('Date', fontsize=18)
    plt.ylabel('Close Price Normalized', fontsize=18)

    filename_path = FLAGS.output_directory + "/" + "company_close_price.png"
    plt.savefig(fname=filename_path)


def plot_company_close_price_real(unified_stock_dataframe_with_date: pd.DataFrame, normalized_dataset, close_min,
                                  close_max,
                                  close_mean, close_std):
    plt.figure(figsize=(18, 9))

    price = normalized_dataset[:, 3]
    real_price = get_real_price(price, close_min, close_max, close_mean, close_std)

    plt.plot(range(normalized_dataset.shape[0]), real_price)
    plt.xticks(range(0, normalized_dataset.shape[0], 500),
               unified_stock_dataframe_with_date[FLAGS.company_ticker_symbol + '_Date'].loc[::500], rotation=45)
    plt.xlabel('Date', fontsize=18)
    plt.ylabel('Close Price Real', fontsize=18)

    filename_path = FLAGS.output_directory + "/" + "company_close_price_real.png"
    plt.savefig(fname=filename_path)


def plot_train_history(history, title):
    loss = history.history['loss']
    val_loss = history.history['val_loss']

    epochs = range(len(loss))

    plt.figure()

    plt.plot(epochs, loss, 'b', label='Training loss')
    plt.plot(epochs, val_loss, 'r', label='Validation loss')
    plt.title(title)
    plt.legend()

    filename_path = FLAGS.output_directory + "/" + "train_validation_loss.png"
    plt.savefig(filename_path)


def plot_train_history_real(history, title, close_min, close_max, close_mean, close_std):
    loss = history.history['loss']
    val_loss = history.history['val_loss']

    loss = get_real_price(loss, close_min, close_max, close_mean, close_std)
    val_loss = get_real_price(val_loss, close_min, close_max, close_mean, close_std)

    epochs = range(len(loss))

    plt.figure()

    plt.plot(epochs, loss, 'b', label='Training loss')
    plt.plot(epochs, val_loss, 'r', label='Validation loss')
    plt.title(title)
    plt.legend()

    filename_path = FLAGS.output_directory + "/" + "train_validation_loss_real.png"
    plt.savefig(filename_path)


def create_time_steps(length):
    return list(range(-length, 0))


"""
    The function plots for a taken sample history-prediction-expected_output:
    -a figure containing the history line and then a set of dots for expected output and actual prediction (optional)
"""


def multi_step_plot(past_values, expected_future, prediction, title_string, figure_name):
    plt.figure(figsize=(18, 9))

    input_indices = create_time_steps(len(past_values))
    output_indices = [-1] + list(range(len(expected_future)))
    new_expected_future = np.concatenate([np.array([np.array(past_values[-1])]), np.array(expected_future)])
    new_prediction = np.concatenate([np.array([np.array(past_values[-1])]), np.array(prediction)])

    plt.plot(input_indices, np.array(past_values), label='History')
    plt.plot(output_indices, np.array(new_expected_future), 'b:',
             label='Expected Future')
    if prediction.any():
        plt.plot(output_indices, np.array(new_prediction), 'r:',
                 label='Predicted Future')
    plt.legend(loc='upper left')
    plt.title(title_string)

    filename_path = FLAGS.output_directory + "/" + figure_name + ".png"
    plt.savefig(filename_path)


"""
    The function plots for a taken sample history-prediction-expected_output:
    -a figure containing the history line and then dots for expected output and actual prediction (optional)
"""


def save_plot(plot_data, delta, title, figure_name):
    labels = ['History', 'True Future', 'Model Prediction']
    marker = ['.-', 'rx', 'go']
    time_steps = create_time_steps(plot_data[0].shape[0])
    if delta:
        future = delta
    else:
        future = 0

    plt.figure(figsize=(18, 9))
    for i, x in enumerate(plot_data):
        if i:
            plt.plot(future, plot_data[i], marker[i], markersize=10,
                     label=labels[i])
        else:
            plt.plot(time_steps, plot_data[i].flatten(), marker[i], label=labels[i])

    plt.legend(loc='upper left')
    plt.xlim([time_steps[0], (future + 5) * 2])
    plt.xlabel('Time-Step')
    plt.title(title)

    filename_path = FLAGS.output_directory + "/" + figure_name + ".png"
    plt.savefig(filename_path)


"""
    Returns a tuple from two arrays:
    - first array contains the input for the network:
        -an array of the past_history size containing the considered features
    - second array contains the "sample output" of the network for comparison:
        -an array of size equal to "future" size 
"""


def multivariate_data(initial_dataset, output_target_dataset,
                      start_index, end_index,
                      past_history_size, output_target_size):
    input_data = []
    output_data = []

    start_index = start_index + past_history_size
    if end_index is None:
        end_index = len(initial_dataset) - output_target_size

    for i in range(start_index, end_index):
        indices = range(i - past_history_size, i)
        input_data.append(initial_dataset[indices])
        output_data.append(output_target_dataset[i: i + output_target_size])

    return np.array(input_data), np.array(output_data)


def singlevariate_data(initial_dataset, output_target_dataset,
                       start_index, end_index,
                       past_history_size, output_target_size):
    input_data = []
    output_data = []

    start_index = start_index + past_history_size
    if end_index is None:
        end_index = len(initial_dataset) - output_target_size

    for i in range(start_index, end_index):
        indices = range(i - past_history_size, i)
        input_data.append(initial_dataset[indices])
        output_data.append(output_target_dataset[i + output_target_size])

    return np.array(input_data), np.array(output_data)


def mean_squared_error_loss(y_actual, y_pred):
    return tf.reduce_mean(tf.square(tf.subtract(y_actual, y_pred)))


def compute_binary_accuracy(x, y, multi_step_model):
    # Shape of xis no_samples:no_past_days_to_use:columns_to_use
    # the fourth column (index 3 contains the close price)
    no_samples = len(x)
    batch_size = 128
    no_wins = 0
    no_losses = 0
    valid_predictions = 0
    batch_iterations = math.ceil(no_samples / batch_size)
    for i in range(batch_iterations):
        last_element = min((i + 1) * batch_size, no_samples)
        current_batch_x = x[i * batch_size:last_element]
        current_batch_y = y[i * batch_size:last_element]

        expected_price_list = current_batch_y
        computed_price_list = multi_step_model.predict(current_batch_x)
        current_close_price_list = []
        for j in range(len(current_batch_x)):
            current_close_price_list.append(current_batch_x[j][-1][3])

        for j in range(0, len(current_close_price_list)):
            current_close_price = current_close_price_list[j]
            expected_price = expected_price_list[j]
            computed_price = computed_price_list[j][0]
            if ((expected_price > current_close_price and computed_price > current_close_price) or
                    expected_price <= current_close_price and computed_price <= current_close_price):
                # print("Current_close_price: {}, Expected_price: {}, Computed_price: {}".format(current_close_price,
                #                                                                               expected_price,
                #                                                                               computed_price))
                valid_predictions += 1
            if expected_price > current_close_price:
                no_wins += 1
            else:
                no_losses += 1

    accuracy = valid_predictions / no_samples
    losses_percent = no_losses / no_samples
    wins_percent = no_wins / no_samples
    return [accuracy, losses_percent, wins_percent]


def predict_all_prices(model, normalized_dataset, past_history_size, future_no_days):
    end_index = len(normalized_dataset) - future_no_days
    start_index = past_history_size

    output_data = []
    for i in range(start_index, end_index):
        indices = range(i - past_history_size, i)
        input_data = normalized_dataset[indices]
        output_price = model.predict(np.expand_dims(input_data, axis=0))[0][0]
        output_data.append(output_price)

    return output_data


def get_all_predicted_price(model, normalized_dataset, past_history_size, future_no_days):
    predicted_prices = normalized_dataset[0:past_history_size + future_no_days, 3]  # initialized first set
    next_predicted_prices = np.array(predict_all_prices(model, normalized_dataset, past_history_size, future_no_days))
    predicted_prices = np.concatenate((predicted_prices, next_predicted_prices), axis=None)
    return predicted_prices


def plot_company_predicted_plot(real_close_prices, predicted_close_prices, unified_stock_dataframe_with_date):
    plt.figure(figsize=(18, 9))
    plt.plot(range(len(real_close_prices)), real_close_prices, color='red', label='real close prices')
    plt.plot(range(len(predicted_close_prices)), predicted_close_prices, color='green', label='predicted close prices')
    plt.xticks(range(0, len(real_close_prices), 500),
               unified_stock_dataframe_with_date[FLAGS.company_ticker_symbol + '_Date'].loc[::500], rotation=45)
    plt.xlabel('Date', fontsize=18)
    plt.ylabel('Close Price Preprocessed', fontsize=18)
    plt.legend(loc="upper left")

    filename_path = FLAGS.output_directory + "/" + "company_predicted_close_price.png"
    plt.savefig(fname=filename_path)


def plot_company_predicted_plot_real(real_close_prices, predicted_close_prices, unified_stock_dataframe_with_date,
                                     close_min, close_max,
                                     close_mean, close_std):
    adapted_real_close_prices = get_real_price(real_close_prices, close_min, close_max, close_mean, close_std)
    adapter_predicted_close_prices = get_real_price(predicted_close_prices, close_min, close_max, close_mean, close_std)

    plt.figure(figsize=(18, 9))
    plt.plot(range(len(adapted_real_close_prices)), adapted_real_close_prices, color='red', label='real close prices')
    plt.plot(range(len(adapter_predicted_close_prices)), adapter_predicted_close_prices, color='green',
             label='predicted close prices')
    plt.xticks(range(0, len(real_close_prices), 500),
               unified_stock_dataframe_with_date[FLAGS.company_ticker_symbol + '_Date'].loc[::500], rotation=45)
    plt.xlabel('Date', fontsize=18)
    plt.ylabel('Close Price Real', fontsize=18)
    plt.legend(loc="upper left")

    filename_path = FLAGS.output_directory + "/" + "company_predicted_close_real_price.png"
    plt.savefig(fname=filename_path)


def model_training(normalized_dataset, unified_stock_dataframe_with_date, close_min, close_max, close_mean, close_std):
    output_target_dataset = normalized_dataset[:, 3]  # it represents the 'Close' column
    training_ending_index = compute_training_split_index(normalized_dataset,
                                                         percentage_split=FLAGS.percentage_split)

    x_train, y_train = singlevariate_data(normalized_dataset,
                                          output_target_dataset,
                                          0,
                                          training_ending_index,
                                          FLAGS.past_history_no_days,
                                          FLAGS.future_prediction_no_days)

    x_validation, y_validation = singlevariate_data(normalized_dataset,
                                                    output_target_dataset,
                                                    training_ending_index,
                                                    None,
                                                    FLAGS.past_history_no_days,
                                                    FLAGS.future_prediction_no_days)

    buffer_size = 1000
    train_data = tf.data.Dataset.from_tensor_slices((x_train, y_train))
    train_data = train_data.cache().shuffle(buffer_size).batch(FLAGS.batch_size).repeat()

    validation_data = tf.data.Dataset.from_tensor_slices((x_validation, y_validation))
    validation_data = validation_data.shuffle(buffer_size).batch(FLAGS.batch_size)

    tf.keras.backend.set_floatx('float64')

    multi_step_model = tf.keras.models.Sequential()
    multi_step_model.add(tf.keras.layers.LSTM(128,
                                              input_shape=x_train.shape[-2:],
                                              dropout=0.2,
                                              return_sequences=True))
    multi_step_model.add(tf.keras.layers.LSTM(64,
                                              dropout=0.3))
    multi_step_model.add(tf.keras.layers.Dense(32))
    multi_step_model.add(tf.keras.layers.Dense(1))
    '''
    multi_step_model = tf.keras.models.Sequential()
    multi_step_model.add(tf.keras.layers.LSTM(128,
                                              input_shape=x_train.shape[-2:]))
    multi_step_model.add(tf.keras.layers.Dense(256,
                                               activation='relu'))
    multi_step_model.add(tf.keras.layers.Dropout(0.3))
    multi_step_model.add(tf.keras.layers.Dense(128,
                                               activation='relu'))
    multi_step_model.add(tf.keras.layers.Dropout(0.3))
    multi_step_model.add(tf.keras.layers.Dense(64,
                                               activation='relu'))
    multi_step_model.add(tf.keras.layers.Dropout(0.3))
    multi_step_model.add(tf.keras.layers.Dense(1))
    '''
    '''
    multi_step_model = tf.keras.models.Sequential()
    multi_step_model.add(tf.keras.layers.Conv1D(filters=64,
                                                kernel_size=3,
                                                activation="relu"))
    multi_step_model.add(tf.keras.layers.Dropout(0.3))
    multi_step_model.add(tf.keras.layers.Conv1D(filters=32,
                                                kernel_size=3,
                                                activation="relu"))
    multi_step_model.add(tf.keras.layers.Dropout(0.3))
    multi_step_model.add(tf.keras.layers.Conv1D(filters=16,
                                                kernel_size=3,
                                                activation="relu"))
    multi_step_model.add(tf.keras.layers.Dropout(0.3))
    multi_step_model.add(tf.keras.layers.LSTM(64,
                                              dropout=0.2))
    multi_step_model.add(tf.keras.layers.Dense(32))
    multi_step_model.add(tf.keras.layers.Dense(1))
    '''

    multi_step_model.compile(optimizer=tf.keras.optimizers.Adam(),
                             loss=mean_squared_error_loss,
                             metrics=[tf.keras.metrics.MeanSquaredError(),
                                      tf.keras.metrics.MeanAbsoluteError()])

    no_samples_per_epoch = math.ceil(len(x_train) / FLAGS.batch_size)
    multi_step_history = multi_step_model.fit(train_data,
                                              epochs=FLAGS.no_epochs,
                                              steps_per_epoch=no_samples_per_epoch,
                                              validation_data=validation_data,
                                              validation_steps=None,
                                              callbacks=[tf.keras.callbacks.ModelCheckpoint(
                                                  filepath=FLAGS.checkpoints_directory + "/model_{epoch}",
                                                  monitor='val_loss',
                                                  save_best_only=True,
                                                  save_weights_only=True,
                                                  verbose=1
                                              )])
    plot_train_history(multi_step_history, 'Multi-Step Training and Validation loss')
    plot_train_history_real(multi_step_history, 'Multi-Step Training and Validation loss real', close_min, close_max, close_mean, close_std)

    best_checkpoint = tf.train.latest_checkpoint(FLAGS.checkpoints_directory)
    multi_step_model.load_weights(best_checkpoint)

    i = 1
    for x, y in validation_data.take(5):
        save_plot([x[0][:, 3].numpy(), y[0].numpy(), multi_step_model.predict(x)[0]],
                  FLAGS.future_prediction_no_days,
                  "Validation",
                  "Validation_" + str(i))
        i += 1

    i = 1
    for x, y in train_data.take(5):
        save_plot([x[0][:, 3].numpy(), y[0].numpy(), multi_step_model.predict(x)[0]],
                  FLAGS.future_prediction_no_days,
                  "Train",
                  "Train_" + str(i))
        i += 1

    [training_accuracy, training_losses_percentage, training_wins_percentage] = compute_binary_accuracy(
        x_train, y_train, multi_step_model)
    [validation_accuracy, validation_losses_percentage, validation_wins_percentage] = compute_binary_accuracy(
        x_validation, y_validation, multi_step_model)
    print("Training accuracy {}% with losses {}% and wins {}%".format(training_accuracy, training_losses_percentage,
                                                                      training_wins_percentage))
    print(
        "Validation accuracy {}% with losses {}% and wins {}%".format(validation_accuracy, validation_losses_percentage,
                                                                      validation_wins_percentage))
    # plot entire plot
    all_predicted_prices = get_all_predicted_price(multi_step_model, normalized_dataset, FLAGS.past_history_no_days,
                                                   FLAGS.future_prediction_no_days)
    plot_company_predicted_plot(output_target_dataset, all_predicted_prices, unified_stock_dataframe_with_date)
    plot_company_predicted_plot_real(output_target_dataset, all_predicted_prices, unified_stock_dataframe_with_date,
                                     close_min, close_max, close_mean, close_std)


def create_preliminary_directories():
    if not os.path.exists(FLAGS.output_directory):
        os.mkdir(FLAGS.output_directory)
    if not os.path.exists(FLAGS.checkpoints_directory):
        os.mkdir(FLAGS.checkpoints_directory)


def main(_argv):
    unified_stock_dataframe = None
    unified_stock_dataframe_with_date = None
    create_preliminary_directories()

    company_ticker_symbol = FLAGS.company_ticker_symbol
    stock_company_dataframe = prepare_company_stock_data(company_ticker_symbol)

    if FLAGS.use_stock_indices:
        stock_index_dataframe = preprocess_stock_indexes()
        unified_stock_dataframe = link_company_with_indices_data(stock_company_dataframe, stock_index_dataframe)
        unified_stock_dataframe_with_date = unified_stock_dataframe.copy(deep=True)
        unified_stock_dataframe = remove_date_columns_from_unified_dataframe(unified_stock_dataframe)
    else:
        unified_stock_dataframe = stock_company_dataframe
        unified_stock_dataframe_with_date = unified_stock_dataframe.copy(deep=True)
        unified_stock_dataframe = remove_date_columns_from_unified_dataframe(unified_stock_dataframe)

    training_ending_index = compute_training_split_index(unified_stock_dataframe_with_date, FLAGS.percentage_split)
    dataset = unified_stock_dataframe.values

    close_min = 0.0
    close_max = 1.0
    close_std = 1.0
    close_mean = 0.0
    if FLAGS.normalize_data != 'none':
        # normalize dataset
        if FLAGS.normalize_data == 'min_max':
            print("Min-Max Normalization")
            close_min = dataset[:training_ending_index, 3].min(axis=0)
            close_max = dataset[:training_ending_index, 3].max(axis=0)
            dataset = normalize_data(dataset, training_ending_index)
        else:
            print("Interval Min-Max Normalization")
            close_min = dataset[:training_ending_index, 3].min(axis=0) * 0.66
            close_max = dataset[:training_ending_index, 3].max(axis=0) * 1.5
            dataset = normalize_data_increased_interval(dataset, training_ending_index)
    if FLAGS.standardize_data:
        print("Standardization")
        close_mean = dataset[:training_ending_index, 3].mean(axis=0)
        close_std = dataset[:training_ending_index, 3].std(axis=0)

        dataset = standardize_data(dataset, training_ending_index)

    plot_company_close_price(unified_stock_dataframe_with_date, dataset)
    plot_company_close_price_real(unified_stock_dataframe_with_date, dataset, close_min, close_max,
                                  close_mean, close_std)
    model_training(dataset, unified_stock_dataframe_with_date, close_min, close_max, close_mean, close_std)


if __name__ == "__main__":
    try:
        app.run(main)
    except SystemExit:
        pass
