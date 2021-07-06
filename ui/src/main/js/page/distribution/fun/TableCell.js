import React from 'react';
import * as PropTypes from 'prop-types';

const TableCell = ({ children }) => (
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

TableCell.propTypes = {
    children: PropTypes.string.isRequired
};

TableCell.defaultProps = {
};

export default TableCell;
