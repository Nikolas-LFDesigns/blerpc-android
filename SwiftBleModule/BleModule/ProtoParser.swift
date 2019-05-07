import Foundation
import CryptoSwift

/// Type-aliases to "convert" protobuf types to Swift types
public typealias TYPE_INT32 = Int32
public typealias TYPE_BOOL = Bool
public typealias TYPE_BYTES = Data

// TODO(#67): Add support little endian encoding/decoding

/// Describes errors appeared during encoding/decoding proto objects
public enum ProtoParserErrors: Error {
    
    /// Called when client tryying to parse unsupported type
    case notSupportedType
    
    /// Called when ewaong data sended to parser
    case wrongData
}

/// Decoder bytes helper
public class ProtoDecoder {
    
    /// Decode data to UInt8
    /// - parameter fromByte: starting byte
    /// - parameter fromByte: data from which need to convert
    /// - returns: converted value UInt8
    private class func decodeUInt8(fromByte: Int, data: Data) -> UInt8 {
        var result: UInt8 = 0
        result <<= 8
        result |= data[fromByte] & 0xFF
        return result
    }
    
    /// Decode data to Int
    /// - parameter fromByte: starting byte
    /// - parameter data: data from which need to convert
    /// - returns: converted value Int
    private class func decodeInt(fromByte: Int, data: Data) -> Int {
        return data.bytes.withUnsafeBufferPointer { ptr in
            (ptr.baseAddress!.withMemoryRebound(to: Int.self, capacity: 1) { ptr in ptr })
        }.pointee
    }
    
    /// Decode data to UInt16
    /// - parameter fromByte: starting byte
    /// - parameter data: data from which need to convert
    /// - returns: converted value IntUInt1632
    private class func decodeUInt16(fromByte: Int, data: Data) -> UInt16 {
        return data.bytes.withUnsafeBufferPointer { ptr in
            (ptr.baseAddress!.withMemoryRebound(to: UInt16.self, capacity: 1) { ptr in ptr })
        }.pointee
    }
    
    /// Decode data to Int16
    /// - parameter fromByte: starting byte
    /// - parameter data: data from which need to convert
    /// - returns: converted value Int16
    private class func decodeInt16(fromByte: Int, data: Data) -> Int16 {
        let subData = data.subdata(in: Range(fromByte..<fromByte+2))
        return subData.bytes.withUnsafeBufferPointer { ptr in
            (ptr.baseAddress!.withMemoryRebound(to: Int16.self, capacity: 1) { ptr in ptr })
        }.pointee
    }
    
    /// Decode data to UInt32
    /// - parameter fromByte: starting byte
    /// - parameter data: data from which need to convert
    /// - returns: converted value UInt32
    private class func decodeUInt32(fromByte: Int, data: Data) -> UInt32 {
        return data.bytes.withUnsafeBufferPointer { ptr in
            (ptr.baseAddress!.withMemoryRebound(to: UInt32.self, capacity: 1) { ptr in ptr })
        }.pointee
    }
    
    /// Decode data to Int32
    /// - parameter fromByte: starting byte
    /// - parameter data: data from which need to convert
    /// - returns: converted value Int32
    private class func decodeInt32(fromByte: Int, data: Data) -> Int32 {
        return data.subdata(in: Range(fromByte..<4)).bytes.withUnsafeBufferPointer { ptr in
            (ptr.baseAddress!.withMemoryRebound(to: Int32.self, capacity: 1) { ptr in ptr })
        }.pointee
    }
    
    /// Decode data to String
    /// - parameter fromByte: starting byte
    /// - parameter toByte: ending byte
    /// - parameter data: data from which need to convert
    /// - returns: converted value String
    private class func decodeString(fromByte: Int, toByte: Int, data: Data) -> String? {
        let subData = data.subdata(in: Range<Int>.init(fromByte...toByte - fromByte))
        return String(data: subData, encoding: String.Encoding.utf8)
    }
    
    /// Decode data to Bool
    /// - parameter fromByte: starting byte
    /// - parameter data: data from which need to convert
    /// - returns: converted value Bool
    private class func decodeBool(fromByte: Int, data: Data) -> Bool {
        return ProtoDecoder.decodeUInt8(fromByte: fromByte, data: data) != 0
    }
    
    /// Decode data to output object
    /// - parameter data: data which need to be converted
    /// - parameter from: from byte inside data
    /// - parameter to: to byte (actually to calculate lenth)
    /// - parameter type: the type of output's object data
    /// - returns: converted object
    public class func decode(data: Data, from: Int, to: Int, type: String) throws -> Any {
        switch type {
        case "TYPE_INT32":
            if from >= data.count {
                throw ProtoParserErrors.wrongData
            } else {
                if to - from == 1 {
                    return Int32(ProtoDecoder.decodeUInt8(fromByte: from, data: data))
                } else if to - from == 2 {
                    return Int32(ProtoDecoder.decodeInt16(fromByte: from, data: data))
                } else {
                    return Int32(ProtoDecoder.decodeInt32(fromByte: from, data: data))
                }
            }
        case "TYPE_BYTES":
            if to >= data.count {
                throw ProtoParserErrors.wrongData
            } else {
                return data.subdata(in: Range.init(NSRange.init(location: from, length: to - from))!)
            }
        case "TYPE_BOOL":
            if from >= data.count {
                throw ProtoParserErrors.wrongData
            } else {
                return ProtoDecoder.decodeBool(fromByte: from, data: data)
            }
        default:
            throw ProtoParserErrors.notSupportedType
        }
    }
    
}

/// Encoder bytes helper
public class ProtoEncoder {
    
    /// Encode Int to Data
    /// - parameter value: value to convert (Int32)
    /// - returns: converted Data
    private class func encodeInt(value: Int32) -> Data? {
        var valueP = value
        let data = NSData(bytes: &valueP, length: 1)
        return data as Data
    }
    
    /// Encode Int to Data
    /// - parameter value: value to convert (Int16)
    /// - returns: converted Data
    private class func encodeInt(value: Int16) -> Data? {
        var valueP = value
        let data = NSData(bytes: &valueP, length: 2)
        return data as Data
    }
    
    /// Encode Int to Data
    /// - parameter value: value to convert (Int32)
    /// - parameter lenth: in how much bytes to encode
    /// - returns: converted Data
    private class func encodeInt(value: Int32, lenth: Int) -> Data? {
        var valueP = value
        let data = NSData(bytes: &valueP, length: lenth)
        return data as Data
    }
    
    /// Encode any object to data
    /// - parameter object: any object to convert
    /// - parameter from: from byte inside data
    /// - parameter to: to byte (actually to calculate lenth)
    /// - parameter type: the type of output's object data
    /// - returns: converted Data
    public class func encode(object: Any, from: Int, to: Int, type: String) throws -> Data {
        switch type {
        case "TYPE_INT32":
            var valueP = object
            let data = NSData(bytes: &valueP, length: to - from)
            return data as Data
        case "TYPE_BYTES":
            return object as! Data
        case "TYPE_BOOL":
            var valueP = object
            let data = NSData(bytes: &valueP, length: 1)
            return data as Data
        default:
            throw ProtoParserErrors.notSupportedType
        }
    }
    
}
