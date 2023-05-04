# NDArray_2 Readme file

Different approach to the NDArray concept using a doubly linked list. This is strictly a component I'm working on to better understand how to create a class with different functionality, better understand nodes and guard clauses. Not exactly meant to be used, unless you can find a use for it.

# Note
Not all methods have been tested. Will be testing and updating as appropriate - 5/4/2023

# Representation
- The user chooses
    - The length of each row (How many entries can be added) (Vertical - y axis)
    - The total amount of rows (How many different 'arrays' there will be with data) (Horizontal - x axi)
- The representation would be that of a matrix where
    - The row "number" will be on the y-axis, starting at "Row 1" and ending at NDArray size - 1
    - The "entries" of each row would lie on the x-axis. Starting at index 0, ending at row length -1
    - Each node will contain:
        - A "row node" to connect the node above and below it with entries as "next"
            - [row 1]
            - [row 2]
            - null
        - An "entry node" to connect the next and previous nodes -> [row n] -><- (0) -><- (1) -><- (2) -> null
        - Each "row node" will have a number assigned it to represent which row number it is, but they will not have data
        - This specific implementation will not allow entry nodes to connect to an entry node above/below
            - NOTE: You can change this if you like, just need to make sure any time a new node is added it connects
        - Instantiation will require wrapper types / references (not primitives)
            - Reason being is I currently do not want to worry about someone entering values such as:
                - "S", 1, 52.0, 'c'
                - But I may implement this later
- To be updated
