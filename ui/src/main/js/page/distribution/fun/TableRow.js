import React from 'react';
import * as PropTypes from 'prop-types';

const TableRow = ({ children }) => (
    <th className="brian-row">
        <div className="brian-header">
            {children}
            <div className="brian-arrows">
                <div className="arrow-up" />
                <div className="arrow-down" />
            </div>
        </div>
    </th>
);

TableRow.propTypes = {
    children: PropTypes.string.isRequired
};

TableRow.defaultProps = {
};

export default TableRow;
