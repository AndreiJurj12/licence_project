def lstm_cell(prev_ct, prev_ht, input_vector):
    combined = prev_ht + input_vector
    ft = forget_layer(combine)
    candidate = candidate_layer(combine)
    it = input_layer(combine)
    
    ct = prev_ct * ft + candidate * it
    ot = output_layer(combine)
    ht = ot * tanh(ct)
    return ht, ct


for input in inputs:
    ct, ht = lstm_cell(ct, ht, input)
