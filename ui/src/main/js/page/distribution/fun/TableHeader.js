import React from 'react';
import * as PropTypes from 'prop-types';

const TableHeader = ({ children }) => (
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

TableHeader.propTypes = {
    children: PropTypes.string.isRequired
};

TableHeader.defaultProps = {
};

export default TableHeader;
