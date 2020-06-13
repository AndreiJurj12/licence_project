from flask import jsonify, request

from app import app
from services.public_service import predict_service


@app.route('/hello')
def hello():
    return "Test"


@app.route('/prediction', methods=['POST'])
def prediction_endpoint():
    try:
        body_content = request.get_json()

        company_ticker_symbol = body_content['companyTickerSymbol']
        company_csv_data_path = body_content['companyCsvDataPath']
        prediction_starting_date = body_content['predictionStartingDay']
        dow_jones_industrial_csv_data_path = body_content['dowJonesIndustrialCsvDataPath']
        nasdaq_composite_csv_data_path = body_content['nasdaqCompositeCsvDataPath']

        [resulted_classification, resulted_regression] = predict_service(company_ticker_symbol,
                                                                         company_csv_data_path,
                                                                         prediction_starting_date,
                                                                         dow_jones_industrial_csv_data_path,
                                                                         nasdaq_composite_csv_data_path)
        return jsonify({
            "classificationResult": resulted_classification,
            "regressionResult": resulted_regression
        }), 200
    except Exception as e:
        print("Exception encountered {}".format(e))
        return jsonify({}), 500
